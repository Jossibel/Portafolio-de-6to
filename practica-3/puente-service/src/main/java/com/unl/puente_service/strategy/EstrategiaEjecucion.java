package com.unl.puente_service.strategy;

// patron Strategy: concurrente o secuencial
public interface EstrategiaEjecucion {
    String nombre();
    void ejecutar(ContextoPuente ctx) throws InterruptedException;
}
