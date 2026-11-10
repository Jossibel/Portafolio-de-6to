// Modulo 04 - Cruce de vehiculos (puente-service)
import { API } from "./config.js";

const CRUCE_MS = 900;

const $ = (id) => document.getElementById(id);

let autos = {};
let cruzaron = 0;
let violaciones = 0;
let carril = 0;
let fuente = null;


function log(texto, etiqueta) {
    const p = document.createElement("p");
    p.innerHTML = `<span>${etiqueta}</span> ${texto}`;
    $("ptLog").appendChild(p);
    $("ptLog").scrollTop = $("ptLog").scrollHeight;
}


function pintarDireccion(d) {
    $("ptDireccion").textContent = d === "NORTE" ? "→ NORTE" : d === "SUR" ? "← SUR" : "LIBRE";
}


function reiniciar() {
    autos = {};
    cruzaron = violaciones = carril = 0;
    $("ptColaNorte").innerHTML = "";
    $("ptColaSur").innerHTML = "";
    $("ptTablero").innerHTML = "";
    $("ptLog").innerHTML = "";
    $("ptEnPuente").textContent = "0 / 0";
    $("ptCruzaron").textContent = 0;
    $("ptViolaciones").textContent = 0;
    pintarDireccion("NINGUNA");
}


function alEvento(e) {
    log(e.mensaje, e.tMs + "ms");
    const a = autos[e.vehiculo];

    if (e.tipo === "LLEGA") {
        const dir = e.vehiculo.startsWith("N") ? "NORTE" : "SUR";
        const el = document.createElement("span");
        el.className = "auto " + dir;
        el.textContent = e.vehiculo;
        autos[e.vehiculo] = { el, dir };
        $(dir === "NORTE" ? "ptColaNorte" : "ptColaSur").appendChild(el);
        return;
    }

    if (e.tipo === "ESPERA" && a) {
        a.el.classList.add("esperando");
    }

    if (e.tipo === "ENTRA" && a) {
        // pasa el auto al puente y se anima el cruce
        a.el.classList.remove("esperando");
        a.el.classList.add("cruza-" + a.dir);
        a.el.style.animationDuration = CRUCE_MS + "ms";
        a.el.style.top = (6 + (carril++ % 4) * 30) + "px";
        $("ptTablero").appendChild(a.el);
    }

    if (e.tipo === "SALE") {
        if (a) a.el.remove();
        cruzaron++;
    }

    if (e.vehiculo !== "SISTEMA") {
        $("ptEnPuente").textContent = e.enNorte + " / " + e.enSur;
        if (e.enNorte > 0 && e.enSur > 0) violaciones++;
        pintarDireccion(e.direccion);
    }

    $("ptCruzaron").textContent = cruzaron;
    $("ptViolaciones").textContent = violaciones;
}


function mostrarVerificacion(r) {
    const item = (ok, texto) => `<li class="${ok ? "si" : "no"}">${ok ? "✔" : "✘"} ${texto}</li>`;
    $("ptVerificacion").innerHTML =
        `<b>${r.verificacionCorrecta ? "CUMPLE EL CRITERIO" : "NO CUMPLE EL CRITERIO"}</b> (política ${r.politica})<ul>` +
        item(r.unaDireccionALaVez, `veces con autos en sentidos opuestos: ${r.violaciones}`) +
        item(r.todosCruzaron, `cruzaron ${r.cruzaronNorte} al norte y ${r.cruzaronSur} al sur`) +
        `<li>espera máxima: norte ${r.esperaMaxNorteMs} ms, sur ${r.esperaMaxSurMs} ms · tiempo total ${r.tiempoTotalMs} ms</li>` +
        "</ul>";
}


function ocupado(si, texto) {
    $("ptProgreso").classList.toggle("active", si);
    $("ptEstado").textContent = texto;
    ["ptIniciar", "ptInanicion", "ptRendimiento"].forEach((id) => ($(id).disabled = si));
}


$("ptIniciar").addEventListener("click", () => {
    if (fuente) fuente.close();
    reiniciar();
    ocupado(true, "Simulación en curso...");

    const params = new URLSearchParams({
        politica: $("ptPolitica").value,
        escenario: $("ptEscenario").value,
        cruceMs: CRUCE_MS
    });

    fuente = new EventSource(`${API.puente}/stream?${params}`);
    let termino = false;

    fuente.addEventListener("evento", (m) => alEvento(JSON.parse(m.data)));

    fuente.addEventListener("resultado", (m) => {
        termino = true;
        fuente.close();
        pintarDireccion("NINGUNA");
        mostrarVerificacion(JSON.parse(m.data));
        ocupado(false, "Simulación terminada");
    });

    fuente.onerror = () => {
        fuente.close();
        if (!termino) {
            log("no se pudo conectar con puente-service", "ERROR");
            ocupado(false, "Microservicio no disponible");
        }
    };
});


$("ptInanicion").addEventListener("click", async () => {
    ocupado(true, "Comparando política base y justa...");
    try {
        const resp = await fetch(`${API.puente}/inanicion`);
        if (!resp.ok) throw new Error();
        const a = await resp.json();
        const fila = (nombre, r) => `<tr><td>${nombre}</td><td>${r.esperaMaxSurMs}</td><td>${r.esperaMaxNorteMs}</td>
            <td>${r.cambiosDireccion}</td><td>${r.tiempoTotalMs}</td><td>${r.violaciones}</td></tr>`;
        $("ptTablaInanicion").querySelector("tbody").innerHTML = fila("Base (guía)", a.base) + fila("Justa (reto)", a.justa);
        $("ptTablaInanicion").hidden = false;
        ocupado(false, `La espera máxima del sur bajó ${a.reduccionEsperaSurPorcentaje} % con la política justa`);
    } catch {
        ocupado(false, "Microservicio no disponible");
    }
});


$("ptRendimiento").addEventListener("click", async () => {
    ocupado(true, "Midiendo 5 repeticiones (unos 24 s)...");
    try {
        const resp = await fetch(`${API.puente}/comparar?repeticiones=5`);
        if (!resp.ok) throw new Error();
        const c = await resp.json();
        let filas = "";
        for (let i = 0; i < c.repeticiones; i++) {
            filas += `<tr><td>${i + 1}</td><td>${c.tiemposSecuencialMs[i]}</td><td>${c.tiemposConcurrenteMs[i]}</td></tr>`;
        }
        filas += `<tr><td><b>Promedio</b></td><td><b>${c.promedioSecuencialMs}</b></td><td><b>${c.promedioConcurrenteMs}</b></td></tr>`;
        $("ptTablaRend").querySelector("tbody").innerHTML = filas;
        $("ptTablaRend").hidden = false;
        ocupado(false, `Concurrente ${c.aceleracion} veces más rápido (${c.mejoraPorcentaje} % menos tiempo)`);
    } catch {
        ocupado(false, "Microservicio no disponible");
    }
});


reiniciar();
