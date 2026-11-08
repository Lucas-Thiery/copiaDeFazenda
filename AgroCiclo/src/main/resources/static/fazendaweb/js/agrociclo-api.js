/* Integração HTTP do AgroCiclo. Requisições mutáveis usam o token CSRF do Spring Security. */
(function () {
  function cookie(name) {
    const row = document.cookie.split('; ').find((item) => item.startsWith(name + '='));
    return row ? decodeURIComponent(row.split('=').slice(1).join('=')) : '';
  }
  async function api(url, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set('Accept', 'application/json');
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
  window.AgroCicloAPI = { api, feedback };
})();
