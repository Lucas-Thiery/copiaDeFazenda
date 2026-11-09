(() => {
    'use strict';

    const { api, selecionarFazenda } = window.AgroCicloAPI;
    const cadastro = document.getElementById('cadastroProprietario');
    const fazenda = document.getElementById('cadastroFazenda');
    const form = cadastro || fazenda;
    const status = document.getElementById('retornoCadastro');
    const submit = form.querySelector('[type="submit"]');
    let ultimoEnvio;
    let primeiraFazenda = false;

    async function enviar(event) {
        event.preventDefault();
        if (submit.disabled) return;
        const dados = Object.fromEntries(new FormData(form));
        if (cadastro && dados.senha !== dados.confirmacao) {
            status.textContent = 'A confirmação precisa ser igual à senha.';
            return;
        }
        submit.disabled = true;
        status.textContent = 'Aguarde…';
        try {
            const csrf = await api('/api/csrf');
            const headers = { [csrf.headerName]: csrf.token };
            if (cadastro) {
                delete dados.confirmacao;
                dados.aceitaTermos = form.elements.aceitaTermos.checked;
                await api('/api/cadastro/proprietario', {
                    method: 'POST', headers, body: JSON.stringify(dados),
                });
                location.assign('/fazendaweb/login.html?cadastro');
                return;
            }

            dados.areaHectares = Number(dados.areaHectares);
            const body = JSON.stringify(dados);
            if (ultimoEnvio?.body !== body) ultimoEnvio = { body, chave: crypto.randomUUID() };
            const endpoint = primeiraFazenda ? '/api/cadastro/primeira-fazenda' : '/api/fazendas';
            const criada = await api(endpoint, {
                method: 'POST', headers, body, idempotencyKey: ultimoEnvio.chave,
            });
            selecionarFazenda(criada.fazendaId);
            location.assign('/fazendaweb/principal.html');
        } catch (error) {
            if (primeiraFazenda) {
                const sessao = await api('/api/sessao').catch(() => null);
                if (sessao?.codFazenda && !sessao.primeiraFazendaPendente) {
                    selecionarFazenda(sessao.codFazenda);
                    location.assign('/fazendaweb/principal.html');
                    return;
                }
            }
            status.textContent = error.message;
            submit.disabled = false;
        }
    }

    form.addEventListener('submit', enviar);
    if (cadastro) {
        submit.disabled = false;
    } else {
        api('/api/sessao').then(sessao => {
            if (sessao.perfil !== 'PROPRIETARIO') {
                status.textContent = 'O cadastro de propriedades está disponível para o proprietário.';
                return;
            }
            primeiraFazenda = sessao.primeiraFazendaPendente;
            if (primeiraFazenda) {
                document.querySelector('h1').textContent = 'Cadastre sua primeira fazenda';
                for (const voltar of document.querySelectorAll('[data-voltar-cadastro]')) voltar.hidden = true;
            }
            submit.disabled = false;
        }).catch(error => { status.textContent = error.message; });
    }
})();
