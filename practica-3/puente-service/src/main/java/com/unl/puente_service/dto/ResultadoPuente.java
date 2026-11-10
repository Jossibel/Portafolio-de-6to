package com.unl.puente_service.dto;

import com.unl.puente_service.model.EventoPuente;

import java.util.List;

public record ResultadoPuente(
        String escenario,
        String modo,
        String politica,
        ConfiguracionPuente configuracion,
        int vehiculosEsperados,
        int cruzaronNorte,
        int cruzaronSur,
        int violaciones, // veces que hubo autos en sentidos opuestos (debe ser 0)
        int maxSimultaneosMismoSentido,
        int cambiosDireccion,
        long esperaMaxNorteMs,
        long esperaMaxSurMs,
        double esperaPromNorteMs,
        double esperaPromSurMs,
        long tiempoTotalMs,
        boolean unaDireccionALaVez,
        boolean todosCruzaron,
        boolean verificacionCorrecta,
        List<EventoPuente> eventos
) {
    public ResultadoPuente sinEventos() {
        return new ResultadoPuente(escenario, modo, politica, configuracion, vehiculosEsperados, cruzaronNorte,
                cruzaronSur, violaciones, maxSimultaneosMismoSentido, cambiosDireccion, esperaMaxNorteMs,
                esperaMaxSurMs, esperaPromNorteMs, esperaPromSurMs, tiempoTotalMs, unaDireccionALaVez,
                todosCruzaron, verificacionCorrecta, List.of());
    }
}
