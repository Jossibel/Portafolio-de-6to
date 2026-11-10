package com.unl.puente_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// escenario "mixto": llegan autos de los dos lados intercalados
// escenario "inanicion": llegan muchos al norte seguidos y pocos al sur
public class ConfiguracionPuente {

    private String escenario = "mixto";
    @Min(0) @Max(50) private int vehiculosNorte = 6;
    @Min(0) @Max(50) private int vehiculosSur = 6;
    @Min(0) @Max(5000) private long tiempoCruceMs = 300;
    @Min(0) @Max(5000) private long intervaloLlegadaMs = 100;
    @Min(1) @Max(20) private int limiteConsecutivos = 3;

    public static ConfiguracionPuente porDefecto() {
        return new ConfiguracionPuente();
    }

    // para mostrar la inanicion del sur con la politica base
    public static ConfiguracionPuente escenarioInanicion() {
        ConfiguracionPuente c = new ConfiguracionPuente();
        c.escenario = "inanicion";
        c.vehiculosNorte = 15;
        c.vehiculosSur = 3;
        c.tiempoCruceMs = 400;
        c.intervaloLlegadaMs = 80;
        return c;
    }

    public ConfiguracionPuente copia() {
        ConfiguracionPuente c = new ConfiguracionPuente();
        c.escenario = escenario;
        c.vehiculosNorte = vehiculosNorte;
        c.vehiculosSur = vehiculosSur;
        c.tiempoCruceMs = tiempoCruceMs;
        c.intervaloLlegadaMs = intervaloLlegadaMs;
        c.limiteConsecutivos = limiteConsecutivos;
        return c;
    }

    public int total() { return vehiculosNorte + vehiculosSur; }

    public String getEscenario() { return escenario; }
    public void setEscenario(String v) { this.escenario = v; }
    public int getVehiculosNorte() { return vehiculosNorte; }
    public void setVehiculosNorte(int v) { this.vehiculosNorte = v; }
    public int getVehiculosSur() { return vehiculosSur; }
    public void setVehiculosSur(int v) { this.vehiculosSur = v; }
    public long getTiempoCruceMs() { return tiempoCruceMs; }
    public void setTiempoCruceMs(long v) { this.tiempoCruceMs = v; }
    public long getIntervaloLlegadaMs() { return intervaloLlegadaMs; }
    public void setIntervaloLlegadaMs(long v) { this.intervaloLlegadaMs = v; }
    public int getLimiteConsecutivos() { return limiteConsecutivos; }
    public void setLimiteConsecutivos(int v) { this.limiteConsecutivos = v; }
}
