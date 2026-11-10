package com.unl.estacionamiento_service.dto;

public class EstadoEstacionamientoResponse {

    private int capacidad;
    private int ocupados;
    private int disponibles;
    private int esperando;

    public EstadoEstacionamientoResponse(
            int capacidad,
            int ocupados,
            int disponibles,
            int esperando
    ) {
        this.capacidad = capacidad;
        this.ocupados = ocupados;
        this.disponibles = disponibles;
        this.esperando = esperando;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getOcupados() {
        return ocupados;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public int getEsperando() {
        return esperando;
    }
}