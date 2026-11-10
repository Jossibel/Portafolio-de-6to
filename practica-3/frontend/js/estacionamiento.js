import {
    API
} from "./config.js";


export async function simularEstacionamiento() {

    const respuesta =
        await fetch(
            `${API.estacionamiento}/simular`,
            {
                method: "POST"
            }
        );


    if (
        !respuesta.ok
    ) {

        throw new Error(
            "No se pudo iniciar la simulación"
        );

    }

}



export async function obtenerEstado() {

    const respuesta =
        await fetch(
            `${API.estacionamiento}/estado`
        );


    if (
        !respuesta.ok
    ) {

        throw new Error(
            "No se pudo consultar el estado"
        );

    }


    return await respuesta.json();

}



export async function obtenerVehiculos() {

    const respuesta =
        await fetch(
            `${API.estacionamiento}/vehiculos`
        );


    if (
        !respuesta.ok
    ) {

        throw new Error(
            "No se pudieron consultar los vehículos"
        );

    }


    return await respuesta.json();

}