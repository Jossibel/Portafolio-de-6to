import {
    simularEstacionamiento,
    obtenerEstado,
    obtenerVehiculos
} from "./estacionamiento.js";


const btnSimular =
    document.getElementById("btnSimular");

const btnActualizar =
    document.getElementById("btnActualizar");

const textoEstado =
    document.getElementById("textoEstado");

const progreso =
    document.getElementById("progresoSimulacion");

const listaVehiculos =
    document.getElementById("listaVehiculos");

const colaVehiculos =
    document.getElementById("colaVehiculos");

const eventLog =
    document.getElementById("eventLog");

const carritoEntrada =
    document.getElementById("carritoEntrada");


let intervaloEstado = null;

let estadosAnteriores = {};

let simulacionActiva = false;



/* ==========================================
   LOG
========================================== */

function agregarLog(texto) {

    const hora =
        new Date().toLocaleTimeString(
            "es-EC",
            {
                hour12: false
            }
        );


    const linea =
        document.createElement("p");


    linea.innerHTML = `
        <span>${hora}</span>
        ${texto}
    `;


    eventLog.appendChild(linea);


    eventLog.scrollTop =
        eventLog.scrollHeight;

}



/* ==========================================
   CONTADORES
========================================== */

function actualizarContador(id, valor) {

    const elemento =
        document.getElementById(id);


    if (
        elemento.textContent
        === String(valor)
    ) {
        return;
    }


    elemento.textContent =
        valor;


    elemento.classList.remove(
        "bump"
    );


    void elemento.offsetWidth;


    elemento.classList.add(
        "bump"
    );

}



/* ==========================================
   ANIMACIÓN CARRITO
========================================== */

function animarCarritoEntrada() {

    carritoEntrada.classList.remove(
        "hidden"
    );


    carritoEntrada.classList.remove(
        "driving"
    );


    void carritoEntrada.offsetWidth;


    carritoEntrada.classList.add(
        "driving"
    );


    setTimeout(
        () => {

            carritoEntrada.classList.add(
                "hidden"
            );

        },
        1300
    );

}



/* ==========================================
   PLAZAS
========================================== */

function pintarEstacionamiento(
    vehiculos
) {

    const dentro =
        vehiculos.filter(
            v =>
                v.estado === "DENTRO"
        );


    for (
        let i = 1;
        i <= 4;
        i++
    ) {

        const slot =
            document.querySelector(
                `#slot-${i} .slot-content`
            );


        slot.innerHTML =
            "";

    }


    dentro
        .slice(0, 4)
        .forEach(
            (vehiculo, index) => {

                const slot =
                    document.querySelector(
                        `#slot-${index + 1} .slot-content`
                    );


                const carro =
                    document.createElement(
                        "div"
                    );


                carro.className =
                    "slot-car";


                carro.dataset.id =
                    `V${vehiculo.id}`;


                slot.appendChild(
                    carro
                );

            }
        );

}



/* ==========================================
   COLA
========================================== */

function pintarCola(
    vehiculos
) {

    const esperando =
        vehiculos.filter(
            v =>
                v.estado === "ESPERANDO"
        );


    colaVehiculos.innerHTML =
        "";


    if (
        esperando.length === 0
    ) {

        colaVehiculos.innerHTML = `
            <span class="queue-empty">
                Sin vehículos esperando
            </span>
        `;

        return;
    }


    esperando.forEach(
        vehiculo => {

            const carro =
                document.createElement(
                    "div"
                );


            carro.className =
                "queue-car";


            carro.textContent =
                `V${vehiculo.id}`;


            colaVehiculos.appendChild(
                carro
            );

        }
    );

}



/* ==========================================
   TARJETAS
========================================== */

function pintarVehiculos(
    vehiculos
) {

    listaVehiculos.innerHTML =
        "";


    if (
        vehiculos.length === 0
    ) {

        listaVehiculos.innerHTML = `

            <div class="empty-state">

                <div class="empty-car">
                    ▰
                </div>

                <p>
                    Presiona iniciar para comenzar la simulación.
                </p>

            </div>
        `;

        return;
    }


    vehiculos.forEach(
        vehiculo => {

            const tarjeta =
                document.createElement(
                    "div"
                );


            tarjeta.className =
                `vehicle-card ${vehiculo.estado}`;


            let textoEstado =
                vehiculo.estado;


            if (
                vehiculo.estado
                === "DENTRO"
            ) {

                textoEstado =
                    "EN PLAZA";

            }


            if (
                vehiculo.estado
                === "ESPERANDO"
            ) {

                textoEstado =
                    "EN COLA";

            }


            if (
                vehiculo.estado
                === "FUERA"
            ) {

                textoEstado =
                    "FINALIZADO";

            }


            tarjeta.innerHTML = `

                <strong>
                    VEHÍCULO ${vehiculo.id}
                </strong>

                <br>

                ${textoEstado}
            `;


            listaVehiculos.appendChild(
                tarjeta
            );

        }
    );

}



/* ==========================================
   DETECTAR CAMBIOS
========================================== */

function detectarCambios(
    vehiculos
) {

    vehiculos.forEach(
        vehiculo => {

            const anterior =
                estadosAnteriores[
                    vehiculo.id
                ];


            if (
                anterior
                === vehiculo.estado
            ) {
                return;
            }


            if (
                vehiculo.estado
                === "DENTRO"
            ) {

                animarCarritoEntrada();


                agregarLog(
                    `Vehículo ${vehiculo.id} obtuvo un espacio.`
                );

            }


            if (
                vehiculo.estado
                === "ESPERANDO"
            ) {

                agregarLog(
                    `Vehículo ${vehiculo.id} espera por Semaphore.acquire().`
                );

            }


            if (
                vehiculo.estado
                === "FUERA"
            ) {

                agregarLog(
                    `Vehículo ${vehiculo.id} liberó un espacio.`
                );

            }


            estadosAnteriores[
                vehiculo.id
            ] =
                vehiculo.estado;

        }
    );

}



/* ==========================================
   ACTUALIZACIÓN
========================================== */

async function actualizarInterfaz() {

    try {

        const estado =
            await obtenerEstado();


        const vehiculos =
            await obtenerVehiculos();


        actualizarContador(
            "capacidad",
            estado.capacidad
        );


        actualizarContador(
            "ocupados",
            estado.ocupados
        );


        actualizarContador(
            "disponibles",
            estado.disponibles
        );


        actualizarContador(
            "esperando",
            estado.esperando
        );


        detectarCambios(
            vehiculos
        );


        pintarEstacionamiento(
            vehiculos
        );


        pintarCola(
            vehiculos
        );


        pintarVehiculos(
            vehiculos
        );


        if (
            simulacionActiva
        ) {

            if (
                estado.ocupados === 0
                &&
                estado.esperando === 0
                &&
                vehiculos.length > 0
            ) {

                finalizarSimulacion();

            } else {

                textoEstado.textContent =
                    `${estado.ocupados} dentro · ${estado.esperando} esperando`;

            }

        }

    } catch (error) {

        console.error(
            error
        );


        textoEstado.textContent =
            "Microservicio no disponible";


        agregarLog(
            "ERROR: no se pudo conectar con backend."
        );

    }

}



/* ==========================================
   SIMULACIÓN
========================================== */

function activarSimulacion() {

    simulacionActiva =
        true;


    estadosAnteriores =
        {};


    btnSimular.disabled =
        true;


    btnSimular.innerHTML = `
        <span class="btn-icon">
            ■
        </span>

        SIMULACIÓN EN CURSO
    `;


    progreso.classList.add(
        "active"
    );


    textoEstado.textContent =
        "Ejecutando procesos concurrentes...";


    agregarLog(
        "Simulación iniciada: 10 vehículos."
    );

}



/* ==========================================
   FINALIZAR
========================================== */

function finalizarSimulacion() {

    simulacionActiva =
        false;


    btnSimular.disabled =
        false;


    btnSimular.innerHTML = `
        <span class="btn-icon">
            ▶
        </span>

        NUEVA SIMULACIÓN
    `;


    progreso.classList.remove(
        "active"
    );


    textoEstado.textContent =
        "Simulación finalizada";


    agregarLog(
        "Proceso completado. Todos los vehículos salieron."
    );


    if (
        intervaloEstado
    ) {

        clearInterval(
            intervaloEstado
        );


        intervaloEstado =
            null;

    }

}



/* ==========================================
   BOTONES
========================================== */

btnSimular.addEventListener(
    "click",
    async () => {

        try {

            activarSimulacion();


            await simularEstacionamiento();


            await actualizarInterfaz();


            intervaloEstado =
                setInterval(
                    actualizarInterfaz,
                    400
                );

        } catch (error) {

            console.error(
                error
            );


            simulacionActiva =
                false;


            btnSimular.disabled =
                false;


            progreso.classList.remove(
                "active"
            );


            textoEstado.textContent =
                "Error iniciando simulación";


            agregarLog(
                "ERROR al iniciar simulación."
            );

        }

    }
);



btnActualizar.addEventListener(
    "click",
    async () => {

        agregarLog(
            "Actualización manual solicitada."
        );


        await actualizarInterfaz();

    }
);



/* ==========================================
   INICIO
========================================== */

actualizarInterfaz();