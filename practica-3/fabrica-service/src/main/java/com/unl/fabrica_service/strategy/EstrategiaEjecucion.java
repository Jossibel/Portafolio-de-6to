package com.unl.fabrica_service.strategy;

// patron Strategy: concurrente o secuencial
public interface EstrategiaEjecucion {
    String nombre();
    void ejecutar(ContextoSimulacion ctx) throws InterruptedException;
}
