package com.unl.fabrica_service.dto;

public record ComparacionRendimiento(
        int repeticiones,
        double promedioSecuencialMs,
        double promedioConcurrenteMs,
        double aceleracion,
        double mejoraPorcentaje,
        long[] tiemposSecuencialMs,
        long[] tiemposConcurrenteMs,
        boolean todasLasEjecucionesCorrectas
) {}
