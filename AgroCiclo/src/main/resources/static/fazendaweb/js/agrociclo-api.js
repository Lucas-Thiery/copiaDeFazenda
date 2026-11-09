/* Integração HTTP do AgroCiclo. Requisições mutáveis usam o token CSRF do Spring Security. */
(function () {
  const chaveFazenda = 'agrociclo.fazenda';

  function fazendaSelecionada() {
    try { return sessionStorage.getItem(chaveFazenda); }
    catch { return null; }
  }

  function selecionarFazenda(id) {
    try {
      if (id === null) sessionStorage.removeItem(chaveFazenda);
      else sessionStorage.setItem(chaveFazenda, String(id));
    } catch { /* A API usa a propriedade padrão quando o armazenamento está indisponível. */ }
  }

  function cookie(name) {
    const row = document.cookie.split('; ').find((item) => item.startsWith(name + '='));
    return row ? decodeURIComponent(row.split('=').slice(1).join('=')) : '';
  }
  async function api(url, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set('Accept', 'application/json');
    const fazenda = fazendaSelecionada();
    if (fazenda && !headers.has('X-Fazenda-Id')) headers.set('X-Fazenda-Id', fazenda);
    if (options.body) headers.set('Content-Type', 'application/json');
    const mutation = ['POST','PUT','PATCH','DELETE'].includes((options.method || 'GET').toUpperCase());
    if (mutation && url !== '/api/setup/inicial' && !headers.has('Idempotency-Key')) {
      headers.set('Idempotency-Key', options.idempotencyKey || crypto.randomUUID());
    }
    const csrf = cookie('XSRF-TOKEN');
    if (csrf) headers.set('X-XSRF-TOKEN', csrf);
    const response = await fetch(url, { credentials: 'same-origin', ...options, headers });
    if (response.status === 401) { location.href = '/fazendaweb/login.html'; throw new Error('Sua sessão expirou. Entre novamente.'); }
    if (response.status === 403) throw new Error('Você não tem permissão para executar esta operação.');
    const contentType = response.headers.get('content-type') || '';
    if (response.status !== 204 && !contentType.includes('application/json')) throw new Error('A resposta do servidor não está no formato esperado. Entre novamente.');
    const data = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) throw new Error(data?.detail || data?.mensagem || data?.erros?.join('; ') || data?.message || 'Não foi possível concluir a operação.');
    return data;
  }
  function feedback(form, message, error = false) {
    let node = form.querySelector('[data-feedback]');
    if (!node) { node = document.createElement('p'); node.dataset.feedback = ''; node.setAttribute('role', 'status'); form.prepend(node); }
    node.textContent = message; node.style.color = error ? '#a12626' : '#24713b';
  }
  async function carregarFazendas() {
    const select = document.querySelector('select.selectFazenda');
    if (!select) return;
    select.setAttribute('aria-label', 'Fazenda ativa');
    select.disabled = true;
    try {
      const sessao = await api('/api/sessao');
      if (sessao.primeiraFazendaPendente) {
        select.replaceChildren(new Option('Cadastre sua primeira fazenda', ''));
        return;
      }
      const fazendas = (await api('/api/fazendas')).filter(fazenda => fazenda.ativa);
      select.replaceChildren(...fazendas.map(fazenda => new Option(fazenda.nome, fazenda.fazendaId)));
      select.value = String(sessao.codFazenda);
      select.disabled = fazendas.length < 2;
      select.addEventListener('change', () => {
        selecionarFazenda(select.value);
        location.reload();
      });
    } catch (error) {
      select.replaceChildren(new Option('Não foi possível carregar fazendas', ''));
      if (fazendaSelecionada()) {
        // A seleção pode pertencer a uma sessão anterior aberta em outra aba.
        const fazendas = await api('/api/fazendas').catch(() => []);
        if (fazendas.length && !fazendas.some(f => f.ativa && String(f.fazendaId) === fazendaSelecionada())) {
          selecionarFazenda(null);
          location.reload();
        }
      }
    }
  }

  window.AgroCicloAPI = { api, feedback, fazendaSelecionada, selecionarFazenda };
  document.addEventListener('DOMContentLoaded', carregarFazendas);
})();
