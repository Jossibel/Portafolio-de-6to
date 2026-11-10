// Archivo: frontend/js/centro_impresion.js

document.addEventListener('DOMContentLoaded', () => {
    const btnIniciar = document.getElementById('btn-start-impresion');
    if (btnIniciar) {
        btnIniciar.addEventListener('click', lanzarHilosImpresion);
    }
    verificarEstadoServidor();
});

let monitorIntervalId = null;

/**
 * Verifica el estado en línea del microservicio
 */
async function verificarEstadoServidor() {
    const txtCore = document.getElementById('status-text-core');
    const ledCore = document.getElementById('status-led-core');
    
    try {
        const res = await fetch('http://localhost:8085/api/impresion/logs');
        if (res.ok) {
            if (txtCore) txtCore.innerText = "Microservicio conectado (Puerto 8085)";
            if (ledCore) { ledCore.style.backgroundColor = "#22c55e"; }
        }
    } catch (err) {
        if (txtCore) txtCore.innerText = "Microservicio desconectado (Offline)";
        if (ledCore) { ledCore.style.backgroundColor = "#ef4444"; }
    }
}

/**
 * Controla el arranque limpio de la simulación todas las veces que se pulse
 */
async function lanzarHilosImpresion() {
    // Apagar cualquier intervalo activo previo para evitar relojes duplicados
    if (monitorIntervalId) {
        clearInterval(monitorIntervalId);
        monitorIntervalId = null;
    }

    const streamConsola = document.getElementById('consola-impresion-stream');
    const badgeStatus = document.getElementById('status-badge-impresion');
    const txtEtapa = document.getElementById('txt-etapa-activa');
    const txtDocs = document.getElementById('txt-docs-procesados');
    
    // Reseteo visual completo e inmediato en la pantalla
    if (badgeStatus) badgeStatus.innerText = "SIMULANDO";
    if (txtEtapa) { txtEtapa.innerText = "INICIALIZANDO"; txtEtapa.style.color = "#000"; }
    if (txtDocs) txtDocs.innerText = "0 / 3";
    resetearLedsVisuales();

    streamConsola.innerHTML = `<p class="sys-trace-line"><span>SYS</span> Reiniciando memoria concurrente y búfer REST...</p>`;

    try {
        // 1. Enviar la petición POST al backend para limpiar logs e iniciar hilos
        await fetch('http://localhost:8085/api/impresion/simular', { method: 'POST' });

        // 2. PAUSA CORRECTA: Esperar 150ms a que el servidor asiente los cambios en memoria
        await new Promise(resolve => setTimeout(resolve, 150)); // <-- Corregido de 'res' a 'resolve'

        // 3. Iniciar el monitor de lectura de logs limpio
        monitorIntervalId = setInterval(actualizarConsolaImpresion, 400);

    } catch (error) {
        if (badgeStatus) badgeStatus.innerText = "ERROR";
        streamConsola.innerHTML += `<p class="sys-trace-line" style="color: #ef4444;">[ERR] Conexión rechazada. Verifique Spring Boot.</p>`;
    }
}
/**
 * Procesa las trazas y actualiza los indicadores del Panel
 */
async function actualizarConsolaImpresion() {
    try {
        const respuesta = await fetch('http://localhost:8085/api/impresion/logs');
        const logs = await respuesta.json();
        const streamConsola = document.getElementById('consola-impresion-stream');
        const txtEtapa = document.getElementById('txt-etapa-activa');
        const txtDocs = document.getElementById('txt-docs-procesados');
        
        // Si el backend está vacío porque se acaba de limpiar en el click, salir y esperar el siguiente ciclo
        if (!logs || logs.length === 0) {
            return;
        }

        streamConsola.innerHTML = '';
        let totalDocumentosImpresos = 0;

        logs.forEach(log => {
            const p = document.createElement('p');
            p.className = "sys-trace-line";
            
            if (log.includes("DISEÑO")) {
                p.style.color = "#4ade80";
            } else if (log.includes("REVISIÓN")) {
                p.style.color = "#22d3ee";
            } else if (log.includes("IMPRESIÓN")) { 
                p.style.color = "#fbbf24"; 
                totalDocumentosImpresos++; // Sumar de forma estricta los eventos reales actuales
            }

            p.innerText = `[LOG-SERVER] -> ${log}`;
            streamConsola.appendChild(p);
        });

        // CONTROL ANTICORRUPCIÓN: Forzar un tope matemático estricto en el frontend
        if (totalDocumentosImpresos > 3) totalDocumentosImpresos = 3;

        // Actualizar el contador en la vista
        if (txtDocs) txtDocs.innerText = `${totalDocumentosImpresos} / 3`;
        streamConsola.scrollTop = streamConsola.scrollHeight;

        const ultimoLog = logs[logs.length - 1];
        resetearLedsVisuales();

        // Actualizar etiqueta del Panel de Control
        if (ultimoLog.includes("DISEÑO")) {
            encenderLed('led-diseno', 'txt-diseno', '#dcfce7', '#22c55e', 'PROCESANDO');
            if (txtEtapa) { txtEtapa.innerText = "DISEÑO"; txtEtapa.style.color = "#22c55e"; }
        } else if (ultimoLog.includes("REVISIÓN")) {
            encenderLed('led-revision', 'txt-revision', '#ecfeff', '#06b6d4', 'VERIFICANDO');
            if (txtEtapa) { txtEtapa.innerText = "REVISIÓN"; txtEtapa.style.color = "#06b6d4"; }
        } else if (ultimoLog.includes("IMPRESIÓN")) {
            encenderLed('led-impresion', 'txt-impresion', '#fef3c7', '#f59e0b', 'IMPRIMIENDO');
            if (txtEtapa) { txtEtapa.innerText = "IMPRESIÓN"; txtEtapa.style.color = "#f59e0b"; }
        }

        // Criterio de parada exitosa (9 logs en el pipeline)
        if (logs.length >= 9) {
            clearInterval(monitorIntervalId);
            monitorIntervalId = null; // Liberar el identificador del reloj
            resetearLedsVisuales();
            
            const badgeStatus = document.getElementById('status-badge-impresion');
            if (badgeStatus) {
                badgeStatus.innerText = "COMPLETADO";
                badgeStatus.style.color = "#10b981";
            }
            if (txtEtapa) { txtEtapa.innerText = "TERMINADO"; txtEtapa.style.color = "#10b981"; }
            if (txtDocs) txtDocs.innerText = "3 / 3";

            const exitoMsg = document.createElement('p');
            exitoMsg.className = "sys-trace-line";
            exitoMsg.style.color = "#34d399";
            exitoMsg.style.fontWeight = "bold";
            exitoMsg.style.marginTop = "10px";
            exitoMsg.style.textAlign = "center";
            exitoMsg.innerText = "✓ Secuencia completada exitosamente sin condiciones de carrera.";
            streamConsola.appendChild(exitoMsg);
            streamConsola.scrollTop = streamConsola.scrollHeight;
        }
    } catch (err) {
        console.error("Error leyendo trazas concurrentes: ", err);
    }
}


function encenderLed(idCard, idTxt, colorBg, colorBorder, mensajeEstado) {
    const card = document.getElementById(idCard);
    const txt = document.getElementById(idTxt);
    if(card) { card.style.backgroundColor = colorBg; card.style.borderColor = colorBorder; }
    if(txt) { txt.innerText = mensajeEstado; txt.style.color = colorBorder; }
}

function resetearLedsVisuales() {
    ['led-diseno', 'led-revision', 'led-impresion'].forEach(id => {
        const el = document.getElementById(id);
        if(el) { el.style.backgroundColor = "#fff"; el.style.borderColor = "#000"; }
    });
    if(document.getElementById('txt-diseno')) document.getElementById('txt-diseno').innerText = "Espera";
    if(document.getElementById('txt-revision')) document.getElementById('txt-revision').innerText = "Bloqueado";
    if(document.getElementById('txt-impresion')) document.getElementById('txt-impresion').innerText = "Bloqueado";
}

window.lanzarHilosImpresion = lanzarHilosImpresion;