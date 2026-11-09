(() => {
    'use strict';

    const { api, feedback, fazendaSelecionada } = window.AgroCicloAPI;
    const find = selector => document.querySelector(selector);
    const profiles = {
        PROPRIETARIO: 'Proprietário',
        GERENTE_CAMPO: 'Gerente de Campo',
        OPERADOR: 'Operador',
    };
    const alertCategories = {
        CONFIGURACAO: 'Configuração',
        SANITARIO: 'Sanidade',
        MANUTENCAO: 'Manutenção',
        VALIDADE: 'Validade',
        VALIDADE_PENDENTE: 'Validade não informada',
        ESTOQUE_MINIMO: 'Estoque mínimo',
    };
    const requests = new WeakMap();
    const owner = 'PROPRIETARIO';
    const alertProfiles = [owner, 'GERENTE_CAMPO'];

    function date(value) {
        return value ? value.split('-').reverse().join('/') : '—';
    }

    function today() {
        const now = new Date();
        return [now.getFullYear(), now.getMonth() + 1, now.getDate()]
            .map(part => String(part).padStart(2, '0')).join('-');
    }

    function table(container, headers, rows) {
        if (!rows.length) {
            container.textContent = 'Nenhum registro encontrado.';
            return;
        }
        const element = document.createElement('table');
        const heading = element.createTHead().insertRow();
        for (const name of headers) {
            const cell = document.createElement('th');
            cell.scope = 'col';
            cell.textContent = name;
            heading.append(cell);
        }
        const body = element.createTBody();
        for (const values of rows) {
            const row = body.insertRow();
            for (const value of values) row.insertCell().textContent = value ?? '—';
        }
        container.replaceChildren(element);
    }

    function bind(form, action) {
        const button = form.querySelector('[type="submit"]');
        button.disabled = false;
        form.addEventListener('submit', async event => {
            event.preventDefault();
            if (button.disabled) return;
            const values = Object.fromEntries(new FormData(form));
            const fields = Array.from(form.elements).filter(field =>
                field !== button && !field.disabled && !field.readOnly);
            button.disabled = true;
            for (const field of fields) field.disabled = true;
            try {
                feedback(form, 'Aguarde…');
                const keepDisabled = await action(values);
                if (keepDisabled) return;
            } catch (error) {
                feedback(form, error.message, true);
            } finally {
                for (const field of fields) field.disabled = false;
            }
            button.disabled = false;
        });
    }

    async function write(form, path, method, data) {
        const body = data === undefined ? undefined : JSON.stringify(data);
        const signature = JSON.stringify([path, method, body]);
        if (requests.get(form)?.signature !== signature) {
            requests.set(form, { signature, key: crypto.randomUUID() });
        }
        const csrf = await api('/api/csrf');
        const response = await api(path, {
            method, body, idempotencyKey: requests.get(form).key,
            headers: { [csrf.headerName]: csrf.token },
        });
        requests.delete(form);
        return response;
    }

    function period(form) {
        form.elements.inicio.defaultValue = today().slice(0, 7) + '-01';
        form.elements.fim.defaultValue = today();
    }

    async function download(endpoint, formato) {
        const headers = new Headers();
        const fazenda = fazendaSelecionada();
        if (fazenda) headers.set('X-Fazenda-Id', fazenda);
        const response = await fetch(endpoint + '&formato=' + formato, { credentials: 'same-origin', headers });
        if (!response.ok) throw new Error('Não foi possível baixar o relatório (HTTP ' + response.status + ').');
        const type = formato === 'pdf' ? 'application/pdf' : 'text/csv';
        if (!response.headers.get('content-type')?.includes(type)) throw new Error('Formato de arquivo inesperado.');
        const objectUrl = URL.createObjectURL(await response.blob());
        const anchor = document.createElement('a');
        anchor.href = objectUrl;
        anchor.download = 'agrociclo-' + endpoint.split('/').pop().split('?')[0] + '.' + formato;
        document.body.append(anchor);
        anchor.click();
        anchor.remove();
        setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
    }

    function reports(form) {
        const result = find('#resultado');
        let reportUrl;
        period(form);
        if (form.elements.tipo) {
            const updateAnimalFilter = () => {
                const animal = form.elements.codAnimal;
                const enabled = form.elements.tipo.value !== 'financeiro';
                animal.disabled = !enabled;
                animal.closest('label').hidden = !enabled;
            };
            updateAnimalFilter();
            form.elements.tipo.addEventListener('change', updateAnimalFilter);
            form.addEventListener('reset', () => setTimeout(updateAnimalFilter, 0));
        }
        bind(form, async values => {
            if (values.fim < values.inicio) throw new Error('A data final deve ser igual ou posterior à inicial.');
            const params = new URLSearchParams({ dtaInicio: values.inicio, dtaFim: values.fim });
            for (const key of ['codSafra', 'codLote', 'codAnimal']) {
                if (values[key]) params.set(key, values[key]);
            }
            const endpoint = '/api/relatorios/' + (values.tipo || form.dataset.report) + '?' + params;
            const report = await api(endpoint);
            find('#filtros').textContent = 'Período: ' + date(report.dtaInicio) + ' a ' + date(report.dtaFim);
            find('#limitacoes').replaceChildren(...report.limitacoes.map(message => {
                const item = document.createElement('li');
                item.textContent = message;
                return item;
            }));
            table(find('#tabela'), report.colunas, report.linhas);
            result.hidden = false;
            reportUrl = endpoint;
            feedback(form, 'Consulta concluída.');
        });
        form.addEventListener('reset', () => {
            reportUrl = undefined;
            result.hidden = true;
            feedback(form, '');
        });
        for (const format of ['csv', 'pdf']) {
            const button = find('#' + format);
            button.addEventListener('click', async () => {
                if (!reportUrl) return;
                button.disabled = true;
                try { await download(reportUrl, format); }
                catch (error) { feedback(form, error.message, true); }
                finally { button.disabled = false; }
            });
        }
    }

    function renderAlerts(items) {
        table(find('#listaAlertas'), ['Categoria', 'Motivo', 'Data de referência', 'Registro'],
            items.map(item => [alertCategories[item.categoria] ?? item.categoria, item.motivo,
                date(item.dtaReferencia), item.codRegistro]));
    }

    function alertList(form) {
        form.elements.referencia.defaultValue = today();
        bind(form, async values => {
            const params = new URLSearchParams({ dtaReferencia: values.referencia });
            if (values.categoria) params.set('categoria', values.categoria);
            const items = await api('/api/alertas?' + params);
            renderAlerts(items);
            find('#resumo').hidden = !items.length;
            find('#resultadoAlertas').hidden = false;
            feedback(form, 'Consulta concluída.');
        });
        form.addEventListener('reset', () => {
            find('#resultadoAlertas').hidden = true;
            feedback(form, '');
        });
    }

    async function alertSettings(form) {
        const path = '/api/configuracoes-alerta';
        const fields = {
            vacinas: 'diasVacina',
            validade: 'diasValidade',
            manutencao: 'diasManutencao',
            horas: 'margemHoras',
            quilometros: 'margemKm',
        };
        let [config] = await api(path);
        for (const [name, key] of Object.entries(fields)) form.elements[name].value = config?.[key] ?? '';
        bind(form, async values => {
            const payload = Object.fromEntries(Object.entries(fields).map(([name, key]) => [key, Number(values[name])]));
            config = await write(form, config ? path + '/atualizar/' + config.configuracaoAlertaId : path,
                config ? 'PUT' : 'POST', payload);
            feedback(form, 'Preferências salvas.');
        });
    }

    async function alertEmail(form) {
        renderAlerts(await api('/api/alertas'));
        bind(form, async () => {
            const response = await write(form, '/api/alertas/enviar', 'POST');
            feedback(form, response.mensagem, response.resultado !== 'ACEITO');
            return response.resultado !== 'FALHA';
        });
    }

    async function registerAccount(form, session) {
        const employees = await api('/api/funcionarios?tamanho=500');
        for (const employee of employees.filter(row => row.ativo)) {
            form.elements.funcionario.add(new Option(employee.nome, employee.funcionarioId));
        }
        bind(form, async values => {
            await write(form, '/api/usuarios', 'POST', {
                codFuncionario: Number(values.funcionario),
                login: values.login.trim(),
                senha: values.senha,
                perfil: values.perfil,
                codFazendas: [session.codFazenda],
                email: values.email.trim() || null,
            });
            form.reset();
            feedback(form, 'Usuário cadastrado.');
        });
    }

    async function deactivateAccount(form, session) {
        const accounts = await api('/api/usuarios?tamanho=500');
        const select = form.elements.conta;
        for (const account of accounts) select.add(new Option(account.login, account.usuarioId));
        const account = () => accounts.find(row => String(row.usuarioId) === select.value);
        function displayAccount() {
            const selected = account();
            find('#perfilAtual').value = profiles[selected?.perfil] || '';
            find('#situacaoConta').value = selected ? (selected.ativo ? 'Ativa' : 'Desativada') : '';
        }
        select.addEventListener('change', () => {
            displayAccount();
            form.elements.confirmacao.checked = false;
        });
        bind(form, async () => {
            const selected = account();
            if (!selected?.ativo) throw new Error('Selecione uma conta ativa.');
            await write(form, '/api/usuarios/' + selected.usuarioId + '/desativacao', 'PATCH');
            selected.ativo = false;
            if (selected.usuarioId === session.usuarioId) {
                location.assign('../login.html');
                return true;
            }
            displayAccount();
            form.elements.confirmacao.checked = false;
            feedback(form, 'Usuário desativado.');
        });
    }

    async function accountEmail(form, session) {
        const path = '/api/usuarios/' + session.usuarioId;
        const account = await api(path);
        form.elements.email.value = account.email ?? '';
        bind(form, async values => {
            await write(form, path + '/email', 'PUT', { email: values.email.trim() });
            feedback(form, 'E-mail atualizado.');
        });
    }

    async function logout(form, session) {
        find('#contaSessao').value = session.login;
        const csrf = await api('/api/csrf');
        const token = document.createElement('input');
        token.type = 'hidden';
        token.name = '_csrf';
        token.value = csrf.token;
        form.append(token);
        form.querySelector('[type="submit"]').disabled = false;
    }

    async function registrosPainel(endpoint) {
        const registros = [];
        const tamanho = 500;
        for (let pagina = 0; ; pagina++) {
            const lote = await api(endpoint + '?pagina=' + pagina + '&tamanho=' + tamanho);
            registros.push(...lote);
            if (lote.length < tamanho) return registros;
        }
    }

    async function graficoAtividade() {
        const hoje = new Date();
        const formato = new Intl.DateTimeFormat('pt-BR', { month: 'short' });
        const meses = Array.from({ length: 7 }, (_, indice) => {
            const mes = new Date(hoje.getFullYear(), hoje.getMonth() - 6 + indice, 1);
            const codigo = mes.getFullYear() + '-' + String(mes.getMonth() + 1).padStart(2, '0');
            return { codigo, rotulo: formato.format(mes).replace('.', ''), total: 0 };
        });
        const query = new URLSearchParams({ dtaInicio: meses[0].codigo + '-01', dtaFim: today() });
        find('#secaoAtividade').hidden = false;
        const legenda = find('#legendaAtividade');
        try {
            const relatorio = await api('/api/relatorios/produtividade?' + query);
            const colunaData = relatorio.colunas.indexOf('data');
            if (colunaData < 0) throw new Error('Data ausente no relatório.');
            const porMes = new Map(meses.map(mes => [mes.codigo, mes]));
            for (const linha of relatorio.linhas) {
                const mes = porMes.get(linha[colunaData]?.slice(0, 7));
                if (mes) mes.total++;
            }
            const maior = Math.max(...meses.map(mes => mes.total));
            if (!maior) {
                legenda.textContent = 'Ainda não há registros de produção neste período.';
                return;
            }
            const barras = meses.map(mes => {
                const coluna = document.createElement('div');
                const barra = document.createElement('div');
                const rotulo = document.createElement('span');
                coluna.className = 'barraWrapper';
                coluna.title = mes.codigo + ': ' + mes.total + ' registros';
                barra.className = 'barra';
                barra.style.height = (mes.total / maior * 82) + '%';
                rotulo.textContent = mes.rotulo + ' · ' + mes.total;
                coluna.append(barra, rotulo);
                return coluna;
            });
            find('#barrasAtividade').replaceChildren(...barras);
            find('#graficoAtividade').hidden = false;
        } catch {
            legenda.textContent = 'Não foi possível carregar os registros de produção.';
        }
    }

    async function alertasPainel() {
        find('[data-alertas]').hidden = false;
        find('#secaoAlertas').hidden = false;
        const estado = find('#estadoAlertas');
        try {
            const alertas = await api('/api/alertas');
            const lista = find('#alertasPainel');
            lista.replaceChildren(...alertas.slice(0, 5).map(alerta => {
                const item = document.createElement('li');
                const categoria = alertCategories[alerta.categoria] ?? alerta.categoria;
                item.textContent = categoria + ' · ' + alerta.motivo;
                return item;
            }));
            lista.hidden = !alertas.length;
            estado.textContent = alertas.length ? '' : 'Nenhum alerta para a fazenda selecionada.';
        } catch {
            estado.textContent = 'Não foi possível carregar os alertas.';
        }
    }

    async function dashboard(session) {
        const indicadores = [
            ['/api/animais', '#totalAnimais', dados => dados.length],
            ['/api/lotes', '#totalLotes', dados => dados.filter(lote => lote.ativo).length + ' lote(s) ativo(s)'],
            ['/api/estoque/itens', '#totalEstoque', dados => dados.filter(item => item.ativo).length],
            ['/api/estoque/itens', '#totalEstoqueBaixo', dados =>
                dados.filter(item => item.ativo && item.abaixoMinimo).length + ' abaixo do mínimo'],
        ];
        const tarefasPendentes = tarefas => tarefas.filter(tarefa => tarefa.status === 'PENDENTE');
        if (alertProfiles.includes(session.perfil)) {
            const inicio = new Date();
            inicio.setDate(inicio.getDate() - ((inicio.getDay() + 6) % 7));
            inicio.setHours(0, 0, 0, 0);
            const fim = new Date(inicio);
            fim.setDate(fim.getDate() + 7);
            indicadores.push(
                ['/api/talhoes', '#totalTalhoes', dados => dados.length],
                ['/api/safras', '#totalSafras', dados => dados.filter(safra => safra.status !== 'ENCERRADA').length + ' safra(s) em aberto'],
                ['/api/tarefas', '#totalTarefas', dados => tarefasPendentes(dados).length],
                ['/api/tarefas', '#tarefasSemana', dados => tarefasPendentes(dados).filter(tarefa => {
                    const prazo = new Date(tarefa.dtaFimPrevisto);
                    return prazo >= inicio && prazo < fim;
                }).length + ' para esta semana'],
            );
        } else {
            find('#tarefasSemana').textContent = 'Consulte suas tarefas no menu lateral.';
        }

        const consultas = new Map();
        const resultados = await Promise.allSettled(indicadores.map(async ([endpoint, seletor, calcular]) => {
            if (!consultas.has(endpoint)) consultas.set(endpoint, registrosPainel(endpoint));
            find(seletor).textContent = calcular(await consultas.get(endpoint));
        }));
        find('#estadoPainel').textContent = resultados.some(resultado => resultado.status === 'rejected')
            ? 'Alguns indicadores não puderam ser carregados.' : '';
        await Promise.all([
            session.perfil === owner ? graficoAtividade() : null,
            alertProfiles.includes(session.perfil) ? alertasPainel() : null,
        ]);
    }

    function showSession(session) {
        const role = profiles[session.perfil] || session.perfil;
        const user = find('.userInfo strong');
        const avatar = find('.userAvatar');
        if (user) user.textContent = session.login + ' · ' + role;
        const novaFazenda = find('[data-proprietario]');
        if (novaFazenda) novaFazenda.hidden = session.perfil !== owner;
        if (avatar) avatar.textContent = session.login.charAt(0).toUpperCase();
        const displayFarm = name => {
            const label = find('.userInfo span');
            if (label) label.textContent = name;
        };
        if (session.primeiraFazendaPendente) {
            displayFarm('Nenhuma fazenda cadastrada');
            return;
        }
        displayFarm('Propriedade #' + session.codFazenda);
        api('/api/fazendas/' + session.codFazenda).then(farm => displayFarm(farm.nome)).catch(() => {});
    }

    async function initialize() {
        const routes = [
            ['[data-report]', [owner], reports],
            ['#consultarAlertas', alertProfiles, alertList],
            ['#configurarAlertas', alertProfiles, alertSettings],
            ['#confirmarResumo', alertProfiles, alertEmail],
            ['#cadastrarUsuario', [owner], registerAccount],
            ['#desativarUsuario', [owner], deactivateAccount],
            ['#emailConta', [owner], accountEmail],
            ['#encerrarSessao', Object.keys(profiles), logout],
        ];
        const route = routes.find(([selector]) => find(selector));
        const form = route && find(route[0]);
        try {
            const session = await api('/api/sessao');
            showSession(session);
            const emailPanel = find('#painelEmail');
            if (emailPanel) {
                if (session.perfil !== owner) return;
                emailPanel.hidden = false;
            }
            if (find('#estadoPainel')) await dashboard(session);
            if (route) {
                const [, allowed, start] = route;
                if (!allowed.includes(session.perfil)) throw new Error('Seu perfil não tem acesso a esta operação.');
                await start(form, session);
            }
        } catch (error) {
            if (form) feedback(form, error.message, true);
            else if (find('#estadoPainel')) find('#estadoPainel').textContent = error.message;
        }
    }

    initialize();
})();
