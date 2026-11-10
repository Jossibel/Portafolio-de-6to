package com.unl.fabrica_service.dto;

import com.unl.fabrica_service.model.EventoSimulacion;

import java.util.List;

public record ResultadoSimulacion(
        String escenario,
        String modo,
        ConfiguracionSimulacion configuracion,
        int piezasEsperadas,
        int piezasProducidas,
        int piezasConsumidas,
        int ocupacionMaxima,
        int inconsistencias,
        int bloqueos,
        long tiempoTotalMs,
        // criterios de verificacion de la guia
        boolean todasConsumidas,
        boolean capacidadRespetada,
        boolean sinInconsistencias,
        boolean verificacionCorrecta,
        List<EventoSimulacion> eventos
) {
    public ResultadoSimulacion sinEventos() {
        return new ResultadoSimulacion(escenario, modo, configuracion, piezasEsperadas, piezasProducidas,
                piezasConsumidas, ocupacionMaxima, inconsistencias, bloqueos, tiempoTotalMs, todasConsumidas,
                capacidadRespetada, sinInconsistencias, verificacionCorrecta, List.of());
    }
}
