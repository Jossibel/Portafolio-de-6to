package com.unl.cajero.model;

public class Operacion {

    private int id;
    private String tipo;
    private double cantidad;
    private String estado;

    public Operacion(int id, String tipo, double cantidad) {
        this.id = id;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.estado = "PENDIENTE";
    }

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}