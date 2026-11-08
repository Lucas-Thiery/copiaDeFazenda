document.addEventListener('DOMContentLoaded', async () => {
  const { api, feedback } = window.AgroCicloAPI;
  try { await api('/api/csrf'); } catch (error) { /* A página de login tratará sessões ausentes. */ }
  for (const select of document.querySelectorAll('select[data-options-from]')) {
    try {
      const rows = await api(select.dataset.optionsFrom);
      select.replaceChildren(new Option('Selecione...', ''));
      for (const row of rows) select.add(new Option(row[select.dataset.optionLabel] + (row.atribuicoes ? ' — ' + row.atribuicoes : ''), Object.entries(row).find(([key]) => key.endsWith("Id"))?.[1]));
    } catch (error) { select.replaceChildren(new Option('Não foi possível carregar opções', '')); }
  }
  for (const form of document.querySelectorAll('form[data-agrociclo-form]')) {
    const date = form.querySelector('input[type="date"]');
    if (date && !date.value && date.name !== 'dataAdmissao') {
      const today = new Date();
      date.value = [today.getFullYear(), String(today.getMonth() + 1).padStart(2, '0'), String(today.getDate()).padStart(2, '0')].join('-');
    }
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      const button = form.querySelector('[type="submit"]'); button.disabled = true;
      const data = Object.fromEntries(new FormData(form));
      for (const key of ['areaHectares','custoHoraReferencia','custoHoraIndividual','peso']) if (data[key] !== undefined && data[key] !== '') data[key] = Number(data[key]);
      for (const key of ['codCargo','codPastagemAtual','codLoteAtual','idadeMeses']) if (data[key] !== undefined && data[key] !== '') data[key] = Number(data[key]);
      for (const key of Object.keys(data)) if (data[key] === '') data[key] = null;
      if (form.dataset.agrocicloForm === '/api/usuarios') { const sessao = await api('/api/sessao'); data.codFazendas = [sessao.codFazenda]; }
      try { await api(form.dataset.agrocicloForm, { method: 'POST', body: JSON.stringify(data), idempotencyKey: form.dataset.requestKey || (form.dataset.requestKey = crypto.randomUUID()) }); feedback(form, 'Cadastro realizado com sucesso.'); form.reset(); delete form.dataset.requestKey; }
      catch (error) { feedback(form, error.message, true); }
      finally { button.disabled = false; }
    });
  }
});

document.querySelectorAll("form").forEach(form => form.addEventListener("input", () => { delete form.dataset.requestKey; }));
