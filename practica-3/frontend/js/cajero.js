// Simulación REST del Ejercicio 2; los resultados siempre provienen del backend.
const API = 'http://localhost:8082/api/cajero';
const $ = id => document.getElementById(id);
const expected = 650;
const money = n => new Intl.NumberFormat('en-US', {style:'currency',currency:'USD',minimumFractionDigits:2}).format(n);
const rows = new Map(Array.from(document.querySelectorAll('.process-row')).map(r => [Number(r.dataset.id),r]));
let busy = false;
let comparison = {mutex:null,unsafe:null};

function log(kind,message){
  const p = document.createElement('p');
  const time = document.createElement('time'); time.textContent = new Date().toLocaleTimeString('es-EC',{hour12:false});
  const mark = document.createElement('span'); mark.textContent = kind;
  p.append(time,mark,document.createTextNode(message)); $('eventLog').append(p);
  $('eventLog').scrollTop = $('eventLog').scrollHeight;
}
function setBusy(value,message){
  busy=value;
  ['btnMutex','btnSinMutex','btnComparar'].forEach(id=>$(id).disabled=value);
  if(message) $('actionStatus').textContent=message;
  $('lockSymbol').classList.toggle('active',value);
}
async function request(path,method='GET'){
  const response = await fetch(`${API}${path}`,{method,headers:{'Accept':'application/json'}});
  if(!response.ok) throw new Error(`HTTP ${response.status} al consultar ${path}`);
  return response.json();
}
async function checkConnection(){
  try{
    const data=await request('/saldo');
    if(typeof data.saldo!=='number') throw new Error('Formato de saldo inválido');
    $('connectionStatus').className='connection online';
    $('connectionStatus').innerHTML='<span class="connection-light"></span>SERVICIO CONECTADO';
    updateSaldo(data.saldo);log('NET','API del cajero disponible en puerto 8082.');
  }catch(error){
    $('connectionStatus').className='connection offline';
    $('connectionStatus').innerHTML='<span class="connection-light"></span>SERVICIO NO DISPONIBLE';
    $('actionStatus').textContent='Inicia cajero-service en localhost:8082 y revisa CORS.';
    log('ERR',error.message);
  }
}
function updateSaldo(value){
  $('saldo').textContent=money(value);$('saldo').classList.remove('pop');void $('saldo').offsetWidth;$('saldo').classList.add('pop');
}
function updateOperations(operations){
  for(const row of rows.values()){
    row.classList.remove('working','done');const state=row.querySelector('.process-state');state.dataset.state='PENDIENTE';state.textContent='PENDIENTE';
  }
  operations.forEach(op=>{
    const row=rows.get(Number(op.id));if(!row)return;
    const state=row.querySelector('.process-state');
    const value=String(op.estado||'DESCONOCIDO').toUpperCase();
    state.dataset.state=value;state.textContent=value;
    if(value==='COMPLETADA')row.classList.add('done');
    if(value==='EJECUTANDO')row.classList.add('working');
  });
}
function renderResult(type,amount){
  const isSafe=type==='mutex';
  $(isSafe?'resultadoMutex':'resultadoSinMutex').textContent=money(amount);
  const detail=$(isSafe?'detalleMutex':'detalleSinMutex');
  detail.textContent=isSafe ? (amount===expected?'Las tres operaciones terminaron con el saldo esperado.': 'Resultado distinto del esperado: revisa el servicio.') : (amount===expected?'Esta ejecución coincidió con el saldo esperado; sin mutex no existe garantía.':`Diferencia respecto a $650: ${money(Math.abs(amount-expected))}. Se evidencia una actualización perdida.`);
  comparison[isSafe?'mutex':'unsafe']=amount;
}
async function runMode(type){
  const safe=type==='mutex';
  setBusy(true, safe?'Ejecutando tres hilos con exclusión mutua…':'Ejecutando demostración sin mutex…');
  log('RUN',safe?'Solicitando simulación con mutex.':'Solicitando simulación sin mutex.');
  try{
    // El POST con mutex espera a los tres hilos antes de responder.
    // Por ello los estados finales se muestran al acabar y no se simula telemetría inexistente.
    const result=await request(safe?'/simular':'/simular-sin-mutex','POST');
    if(typeof result.saldo!=='number')throw new Error('Respuesta de saldo inválida');
    renderResult(type,result.saldo);
    log('OK',`${safe?'Con mutex':'Sin mutex'}: saldo devuelto ${money(result.saldo)}.`);
    if(safe){
      const [operations,state]=await Promise.all([request('/operaciones'),request('/saldo')]);
      updateSaldo(state.saldo);
      updateOperations(operations);
      log('DATA',`Historial obtenido: ${operations.length} operaciones.`);
      $('balanceHint').textContent='RESULTADO CON MUTEX';
    }
    $('lockCaption').textContent=safe?'El mutex protege cada modificación del saldo.':'Esta prueba no protege las lecturas ni las escrituras del saldo.';
    $('actionStatus').textContent=`Prueba ${safe?'con':'sin'} mutex completada: ${money(result.saldo)}.`;
    return true;
  }catch(error){log('ERR',error.message);$('actionStatus').textContent=`Error: ${error.message}`;return false;}
  finally{setBusy(false);}
}
$('btnMutex').addEventListener('click',()=>{if(!busy)void runMode('mutex')});
$('btnSinMutex').addEventListener('click',()=>{if(!busy)void runMode('unsafe')});
$('btnComparar').addEventListener('click',async()=>{
  if(busy)return;
  // Cada llamada se espera antes de ejecutar la siguiente.
  const ok=await runMode('mutex');if(ok)await runMode('unsafe');
});
$('btnLimpiarLog').addEventListener('click',()=>{$('eventLog').replaceChildren();log('SYS','Registro reiniciado.')});
void checkConnection();
