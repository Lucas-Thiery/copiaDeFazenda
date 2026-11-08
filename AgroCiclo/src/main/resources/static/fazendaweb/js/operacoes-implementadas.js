document.addEventListener('DOMContentLoaded',async()=>{
 const {api}=window.AgroCicloAPI;const result=document.getElementById('retorno');
 async function load(select){try{const rows=await api(select.dataset.fetch);select.replaceChildren(new Option('Selecione...', ''));for(const row of rows)select.add(new Option(row[select.dataset.label],String(Object.entries(row).find(([key]) => key.endsWith("Id"))?.[1])));}catch(e){select.replaceChildren(new Option('Erro: '+e.message,''));}}
 try{await api('/api/csrf');}catch(e){result.textContent='É necessário entrar no sistema: '+e.message;}
 for(const select of document.querySelectorAll('[data-fetch]'))await load(select);
 for(const input of document.querySelectorAll('input[type=date]'))input.value=new Date().toLocaleDateString('en-CA');
 for(const form of document.querySelectorAll('form'))form.addEventListener('submit',async e=>{
  e.preventDefault();const button=form.querySelector('button');button.disabled=true;
  const payload=Object.fromEntries(new FormData(form));for(const key of Object.keys(payload))if(payload[key]==='')payload[key]=null;
  let endpoint=form.dataset.endpoint; if(form.dataset.action==='pesagem'){endpoint='/api/pesagens';payload.codAnimal=payload.animalId;delete payload.animalId;}
  if(form.dataset.action==='saida'){endpoint='/api/estoque/movimentacoes';payload.codPartida=payload.partidaId;delete payload.partidaId;}
  try{const response=await api(endpoint,{method:'POST',body:JSON.stringify(payload),idempotencyKey: form.dataset.requestKey || (form.dataset.requestKey = crypto.randomUUID())});result.textContent='Operação concluída.';if(endpoint==='/api/estoque/itens')await Promise.all([...document.querySelectorAll('[data-fetch="/api/estoque/itens"]')].map(load));form.reset(); delete form.dataset.requestKey;}
  catch(err){result.textContent='Falha: '+err.message;}finally{button.disabled=false;}
 });
});
document.querySelectorAll("form").forEach(form => form.addEventListener("input", () => { delete form.dataset.requestKey; }));
