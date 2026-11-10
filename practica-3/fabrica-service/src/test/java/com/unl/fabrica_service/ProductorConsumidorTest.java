package com.unl.fabrica_service;

import com.unl.fabrica_service.dto.ConfiguracionSimulacion;
import com.unl.fabrica_service.dto.ResultadoSimulacion;
import com.unl.fabrica_service.service.SimulacionService;
import com.unl.fabrica_service.strategy.EstrategiaConcurrente;
import com.unl.fabrica_service.strategy.EstrategiaSecuencial;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// pruebas de los criterios de verificacion del ejercicio 3
class ProductorConsumidorTest {

    private final SimulacionService servicio =
            new SimulacionService(List.of(new EstrategiaConcurrente(), new EstrategiaSecuencial()));

    private ConfiguracionSimulacion rapida() {
        ConfiguracionSimulacion c = ConfiguracionSimulacion.porDefecto();
        c.setTiempoFabricacionMs(5);
        c.setTiempoEnsambladoMs(8);
        c.setTimeoutBloqueoMs(300);
        return c;
    }

    // Criterio: "Verificar que el almacén nunca supere 5 ni quede con inconsistencias", varias ejecuciones.
    @RepeatedTest(20)
    void concurrenteCumpleLosCriterios() {
        ResultadoSimulacion r = servicio.simular(rapida(), "concurrente");
        assertEquals(10, r.piezasProducidas());
        assertEquals(10, r.piezasConsumidas(), "Todos los productos producidos son consumidos");
        assertTrue(r.ocupacionMaxima() <= 5, "El almacén nunca supera 5");
        assertEquals(0, r.inconsistencias());
        assertEquals(0, r.bloqueos());
        assertTrue(r.verificacionCorrecta());
    }

    @Test
    void secuencialTambienEsCorrecto() {
        ResultadoSimulacion r = servicio.simular(rapida(), "secuencial");
        assertTrue(r.verificacionCorrecta());
        assertEquals(1, r.ocupacionMaxima(), "En secuencial nunca hay más de una pieza esperando");
    }

    // Pregunta de análisis: espacios = 0 provoca interbloqueo.
    @Test
    void espaciosEnCeroProvocaInterbloqueo() {
        ConfiguracionSimulacion c = rapida();
        c.setEspaciosIniciales(0);
        ResultadoSimulacion r = servicio.simular(c, "concurrente");
        assertEquals(0, r.piezasProducidas());
        assertEquals(0, r.piezasConsumidas());
        assertEquals(4, r.bloqueos(), "Los 2 productores y los 2 consumidores quedan bloqueados");
        assertFalse(r.verificacionCorrecta());
    }

    // Pregunta de análisis: elementos = 5 provoca lecturas de un almacén vacío.
    @Test
    void elementosEnCincoProvocaInconsistencias() {
        ConfiguracionSimulacion c = rapida();
        c.setElementosIniciales(5);
        ResultadoSimulacion r = servicio.simular(c, "concurrente");
        assertTrue(r.inconsistencias() > 0, "Los consumidores leen piezas que no existen");
        assertFalse(r.verificacionCorrecta());
    }
}
