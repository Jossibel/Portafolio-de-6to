package com.unl.estacionamiento_service.model;

public class Vehiculo {

    private int id;
    private String estado;

    public Vehiculo(int id, String estado) {
        this.id = id;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}