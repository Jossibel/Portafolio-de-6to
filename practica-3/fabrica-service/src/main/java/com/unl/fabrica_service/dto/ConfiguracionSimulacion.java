package com.unl.fabrica_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// valores por defecto = los de la guia
public class ConfiguracionSimulacion {

    @Min(1) @Max(20) private int productores = 2;
    @Min(1) @Max(20) private int consumidores = 2;
    @Min(1) @Max(100) private int piezasPorProductor = 5;
    @Min(1) @Max(50) private int capacidad = 5;
    @Min(0) @Max(50) private int espaciosIniciales = 5;
    @Min(0) @Max(50) private int elementosIniciales = 0;
    @Min(0) @Max(5000) private long tiempoFabricacionMs = 100;
    @Min(0) @Max(5000) private long tiempoEnsambladoMs = 150;
    @Min(100) @Max(30000) private long timeoutBloqueoMs = 2000;

    public static ConfiguracionSimulacion porDefecto() {
        return new ConfiguracionSimulacion();
    }

    public ConfiguracionSimulacion copia() {
        ConfiguracionSimulacion c = new ConfiguracionSimulacion();
        c.productores = productores;
        c.consumidores = consumidores;
        c.piezasPorProductor = piezasPorProductor;
        c.capacidad = capacidad;
        c.espaciosIniciales = espaciosIniciales;
        c.elementosIniciales = elementosIniciales;
        c.tiempoFabricacionMs = tiempoFabricacionMs;
        c.tiempoEnsambladoMs = tiempoEnsambladoMs;
        c.timeoutBloqueoMs = timeoutBloqueoMs;
        return c;
    }

    public int totalPiezas() {
        return productores * piezasPorProductor;
    }

    public int getProductores() { return productores; }
    public void setProductores(int v) { productores = v; }
    public int getConsumidores() { return consumidores; }
    public void setConsumidores(int v) { consumidores = v; }
    public int getPiezasPorProductor() { return piezasPorProductor; }
    public void setPiezasPorProductor(int v) { piezasPorProductor = v; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int v) { capacidad = v; }
    public int getEspaciosIniciales() { return espaciosIniciales; }
    public void setEspaciosIniciales(int v) { espaciosIniciales = v; }
    public int getElementosIniciales() { return elementosIniciales; }
    public void setElementosIniciales(int v) { elementosIniciales = v; }
    public long getTiempoFabricacionMs() { return tiempoFabricacionMs; }
    public void setTiempoFabricacionMs(long v) { tiempoFabricacionMs = v; }
    public long getTiempoEnsambladoMs() { return tiempoEnsambladoMs; }
    public void setTiempoEnsambladoMs(long v) { tiempoEnsambladoMs = v; }
    public long getTimeoutBloqueoMs() { return timeoutBloqueoMs; }
    public void setTimeoutBloqueoMs(long v) { timeoutBloqueoMs = v; }
}
