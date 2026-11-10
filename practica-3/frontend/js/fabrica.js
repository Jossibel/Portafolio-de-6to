// Modulo 03 - Productor / Consumidor (fabrica-service)
import { API } from "./config.js";

const CAPACIDAD = 5;
const PIEZAS = 5; // piezas por productor y por consumidor

const $ = (id) => document.getElementById(id);

let actores = {};
let piezas = [];
let producidas = 0;
let consumidas = 0;
let maxima = 0;
let fuente = null;


function log(texto, etiqueta) {
    const p = document.createElement("p");
    p.innerHTML = `<span>${etiqueta}</span> ${texto}`;
    $("pcLog").appendChild(p);
    $("pcLog").scrollTop = $("pcLog").scrollHeight;
}


function crearActor(id, contenedor) {
    const div = document.createElement("div");
    div.className = "actor " + (id[0] === "P" ? "productor" : "consumidor");
    div.innerHTML = `<b>${id[0] === "P" ? "PRODUCTOR" : "CONSUMIDOR"} ${id}</b>
        <span class="estado">en espera</span><br><small class="cuenta">0 piezas</small>`;
    contenedor.appendChild(div);
    actores[id] = { div, cuenta: 0 };
}


function estadoActor(id, clase, texto) {
    const a = actores[id];
    if (!a) return;
    a.div.className = "actor " + (id[0] === "P" ? "productor " : "consumidor ") + clase;
    a.div.querySelector(".estado").textContent = texto;
    a.div.querySelector(".cuenta").textContent = a.cuenta + " piezas";
}


function pintarAlmacen(nueva) {
    const cont = $("pcAlmacen");
    cont.innerHTML = "";
    const total = Math.max(CAPACIDAD, piezas.length);
    for (let i = 0; i < total; i++) {
        const r = document.createElement("div");
        r.className = "ranura" + (i >= CAPACIDAD ? " desborde" : "");
        const p = piezas[i];
        if (p) {
            r.innerHTML = `<div class="caja ${p.productor}${p.id === nueva ? " nueva" : ""}">#${p.id}</div>`;
        }
        cont.appendChild(r);
    }
    $("pcOcupacion").textContent = piezas.length + " / " + CAPACIDAD;
}


function contadores() {
    $("pcProducidas").textContent = producidas;
    $("pcConsumidas").textContent = consumidas;
    $("pcMaxima").textContent = maxima;
}


function reiniciar() {
    actores = {};
    piezas = [];
    producidas = consumidas = maxima = 0;
    $("pcProductores").innerHTML = "";
    $("pcConsumidores").innerHTML = "";
    crearActor("P1", $("pcProductores"));
    crearActor("P2", $("pcProductores"));
    crearActor("C1", $("pcConsumidores"));
    crearActor("C2", $("pcConsumidores"));
    $("pcLog").innerHTML = "";
    $("pcTiempo").textContent = "-";
    contadores();
    pintarAlmacen();
}


function alEvento(e) {
    log(e.mensaje, e.tMs + "ms");
    const a = actores[e.actor];

    switch (e.tipo) {
        case "FABRICA":
            estadoActor(e.actor, "trabajando", "fabricó #" + e.piezaId);
            break;
        case "ESPERA_ESPACIO":
            estadoActor(e.actor, "espera", "esperando espacio");
            break;
        case "INSERTA":
            piezas.push({ id: e.piezaId, productor: e.actor });
            a.cuenta++;
            producidas++;
            maxima = Math.max(maxima, piezas.length);
            estadoActor(e.actor, a.cuenta >= PIEZAS ? "terminado" : "trabajando",
                a.cuenta >= PIEZAS ? "terminó" : "fabricando...");
            break;
        case "ESPERA_PIEZA":
            estadoActor(e.actor, "espera", "esperando pieza");
            break;
        case "EXTRAE":
            piezas = piezas.filter((p) => p.id !== e.piezaId);
            a.cuenta++;
            consumidas++;
            estadoActor(e.actor, "trabajando", "ensamblando #" + e.piezaId);
            break;
        case "ENSAMBLA":
            estadoActor(e.actor, a.cuenta >= PIEZAS ? "terminado" : "", "ensambló #" + e.piezaId);
            break;
        case "INCONSISTENCIA":
            estadoActor(e.actor, "bloqueado", "error: almacén vacío");
            break;
        case "BLOQUEO":
            estadoActor(e.actor, "bloqueado", "bloqueado");
            break;
    }

    contadores();
    pintarAlmacen(e.tipo === "INSERTA" ? e.piezaId : null);
}


function mostrarVerificacion(r) {
    const item = (ok, texto) => `<li class="${ok ? "si" : "no"}">${ok ? "✔" : "✘"} ${texto}</li>`;
    $("pcVerificacion").innerHTML =
        `<b>${r.verificacionCorrecta ? "CUMPLE EL CRITERIO" : "NO CUMPLE EL CRITERIO"}</b> (modo ${r.modo})<ul>` +
        item(r.todasConsumidas, `piezas consumidas: ${r.piezasConsumidas} de ${r.piezasEsperadas}`) +
        item(r.capacidadRespetada, `ocupación máxima del almacén: ${r.ocupacionMaxima} (capacidad ${r.configuracion.capacidad})`) +
        item(r.sinInconsistencias, `inconsistencias: ${r.inconsistencias}, hilos bloqueados: ${r.bloqueos}`) +
        "</ul>";
}


function terminar(texto) {
    $("pcEstado").textContent = texto;
    $("pcProgreso").classList.remove("active");
    $("pcIniciar").disabled = false;
}


$("pcIniciar").addEventListener("click", () => {
    if (fuente) fuente.close();
    reiniciar();
    $("pcIniciar").disabled = true;
    $("pcProgreso").classList.add("active");
    $("pcEstado").textContent = "Simulación en curso...";

    const params = new URLSearchParams({
        modo: $("pcModo").value,
        espacios: $("pcEspacios").value,
        elementos: $("pcElementos").value
    });

    // Server-Sent Events: el servicio manda cada evento de los hilos
    fuente = new EventSource(`${API.productorConsumidor}/stream?${params}`);
    let termino = false;

    fuente.addEventListener("evento", (m) => alEvento(JSON.parse(m.data)));

    fuente.addEventListener("resultado", (m) => {
        termino = true;
        fuente.close();
        const r = JSON.parse(m.data);
        $("pcTiempo").textContent = r.tiempoTotalMs;
        mostrarVerificacion(r);
        terminar("Simulación terminada");
    });

    fuente.onerror = () => {
        fuente.close();
        if (!termino) {
            log("no se pudo conectar con fabrica-service", "ERROR");
            terminar("Microservicio no disponible");
        }
    };
});


$("pcRendimiento").addEventListener("click", async () => {
    $("pcRendimiento").disabled = true;
    $("pcProgreso").classList.add("active");
    $("pcEstado").textContent = "Midiendo 5 repeticiones (unos 17 s)...";
    try {
        const resp = await fetch(`${API.productorConsumidor}/comparar?repeticiones=5`);
        if (!resp.ok) throw new Error();
        const c = await resp.json();
        let filas = "";
        for (let i = 0; i < c.repeticiones; i++) {
            filas += `<tr><td>${i + 1}</td><td>${c.tiemposSecuencialMs[i]}</td><td>${c.tiemposConcurrenteMs[i]}</td></tr>`;
        }
        filas += `<tr><td><b>Promedio</b></td><td><b>${c.promedioSecuencialMs}</b></td><td><b>${c.promedioConcurrenteMs}</b></td></tr>`;
        $("pcTablaRend").querySelector("tbody").innerHTML = filas;
        $("pcTablaRend").hidden = false;
        $("pcEstado").textContent = `Concurrente ${c.aceleracion} veces más rápido (${c.mejoraPorcentaje} % menos tiempo)`;
    } catch {
        $("pcEstado").textContent = "Microservicio no disponible";
    }
    $("pcProgreso").classList.remove("active");
    $("pcRendimiento").disabled = false;
});


document.querySelectorAll("[data-pc]").forEach((b) => {
    b.addEventListener("click", () => {
        const [esp, ele] = b.dataset.pc.split(",");
        $("pcEspacios").value = esp;
        $("pcElementos").value = ele;
    });
});


reiniciar();
