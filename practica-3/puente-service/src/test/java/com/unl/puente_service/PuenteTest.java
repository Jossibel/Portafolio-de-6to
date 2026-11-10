package com.unl.puente_service;

import com.unl.puente_service.dto.AnalisisInanicion;
import com.unl.puente_service.dto.ConfiguracionPuente;
import com.unl.puente_service.dto.ResultadoPuente;
import com.unl.puente_service.service.PuenteService;
import com.unl.puente_service.strategy.EstrategiaConcurrente;
import com.unl.puente_service.strategy.EstrategiaSecuencial;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas del criterio de verificación del Ejercicio 4 y del reto anti-inanición.
class PuenteTest {

    private final PuenteService servicio =
            new PuenteService(List.of(new EstrategiaConcurrente(), new EstrategiaSecuencial()));

    private ConfiguracionPuente rapida() {
        ConfiguracionPuente c = ConfiguracionPuente.porDefecto();
        c.setTiempoCruceMs(30);
        c.setIntervaloLlegadaMs(10);
        return c;
    }

    // Criterio: "Comprobar que nunca crucen vehículos en sentidos opuestos simultáneamente".
    @RepeatedTest(20)
    void politicaBaseNuncaMezclaSentidos() {
        ResultadoPuente r = servicio.simular(rapida(), "concurrente", "base");
        assertEquals(0, r.violaciones());
        assertTrue(r.todosCruzaron());
    }

    @RepeatedTest(20)
    void politicaJustaNuncaMezclaSentidos() {
        ResultadoPuente r = servicio.simular(rapida(), "concurrente", "justa");
        assertEquals(0, r.violaciones());
        assertTrue(r.todosCruzaron());
    }

    @Test
    void concurrenteAprovechaElPuente() {
        ResultadoPuente r = servicio.simular(rapida(), "concurrente", "base");
        assertTrue(r.maxSimultaneosMismoSentido() > 1, "Varios vehículos del mismo sentido cruzan a la vez");
    }

    // Reto: la política justa reduce la espera del lado que sufría inanición.
    @Test
    void politicaJustaReduceLaInanicionDelSur() {
        AnalisisInanicion a = servicio.analisisInanicion();
        assertTrue(a.base().verificacionCorrecta());
        assertTrue(a.justa().verificacionCorrecta());
        assertTrue(a.justa().esperaMaxSurMs() < a.base().esperaMaxSurMs());
    }
}
