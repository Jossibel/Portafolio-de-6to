package com.unl.fabrica_service.service;

import com.unl.fabrica_service.dto.ComparacionRendimiento;
import com.unl.fabrica_service.dto.ConfiguracionSimulacion;
import com.unl.fabrica_service.dto.ResultadoSimulacion;
import com.unl.fabrica_service.model.Almacen;
import com.unl.fabrica_service.model.TipoEvento;
import com.unl.fabrica_service.observer.ObservadorSimulacion;
import com.unl.fabrica_service.observer.PublicadorEventos;
import com.unl.fabrica_service.observer.RegistroEventos;
import com.unl.fabrica_service.strategy.ContextoSimulacion;
import com.unl.fabrica_service.strategy.EstrategiaEjecucion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimulacionService {

    private static final Logger log = LoggerFactory.getLogger("fabrica");

    private final Map<String, EstrategiaEjecucion> estrategias = new HashMap<>();

    public SimulacionService(List<EstrategiaEjecucion> lista) {
        for (EstrategiaEjecucion e : lista) {
            estrategias.put(e.nombre(), e);
        }
    }

    public ResultadoSimulacion simular(ConfiguracionSimulacion cfg, String modo) {
        return simular(cfg, modo, "personalizado", null);
    }

    public ResultadoSimulacion simular(ConfiguracionSimulacion cfg, String modo, String escenario, ObservadorSimulacion extra) {
        EstrategiaEjecucion estrategia = estrategias.get(modo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Modo no válido: " + modo + " (use concurrente o secuencial)");
        }

        PublicadorEventos eventos = new PublicadorEventos();
        RegistroEventos registro = new RegistroEventos();
        eventos.suscribir(registro);
        eventos.suscribir(e -> log.info("[{} ms] [{}] {} | almacén={}", e.tMs(), e.hilo(), e.mensaje(), e.ocupacion()));
        if (extra != null) eventos.suscribir(extra);

        Almacen almacen = new Almacen(cfg.getCapacidad(), cfg.getEspaciosIniciales(), cfg.getElementosIniciales(),
                cfg.getTimeoutBloqueoMs(), eventos);
        ContextoSimulacion ctx = new ContextoSimulacion(cfg, almacen, eventos);

        eventos.publicar(TipoEvento.INICIO, "SISTEMA", null, 0, "Inicio " + modo + " | espacios="
                + cfg.getEspaciosIniciales() + " elementos=" + cfg.getElementosIniciales());

        long inicio = System.nanoTime();
        try {
            estrategia.ejecutar(ctx);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulación interrumpida");
        }
        long tiempoMs = (System.nanoTime() - inicio) / 1_000_000;

        int esperadas = cfg.totalPiezas();
        int producidas = ctx.getProducidas().get();
        int consumidas = ctx.getConsumidas().get();

        // criterios de la tabla de resultados esperados
        boolean todasConsumidas = producidas == esperadas && consumidas == esperadas;
        boolean capacidadOk = almacen.getOcupacionMaxima() <= cfg.getCapacidad();
        boolean sinErrores = almacen.getInconsistencias() == 0 && almacen.getBloqueos() == 0 && almacen.ocupacion() == 0;

        eventos.publicar(TipoEvento.FIN, "SISTEMA", null, almacen.ocupacion(),
                "Fin " + modo + " en " + tiempoMs + " ms | producidas=" + producidas + " consumidas=" + consumidas);

        return new ResultadoSimulacion(escenario, modo, cfg, esperadas, producidas, consumidas,
                almacen.getOcupacionMaxima(), almacen.getInconsistencias(), almacen.getBloqueos(), tiempoMs,
                todasConsumidas, capacidadOk, sinErrores, todasConsumidas && capacidadOk && sinErrores,
                registro.getEventos());
    }

    // secuencial vs concurrente con los valores de la guia
    public ComparacionRendimiento comparar(ConfiguracionSimulacion cfg, int repeticiones) {
        long[] sec = new long[repeticiones];
        long[] con = new long[repeticiones];
        boolean todasOk = true;
        for (int i = 0; i < repeticiones; i++) {
            ResultadoSimulacion s = simular(cfg.copia(), "secuencial");
            ResultadoSimulacion c = simular(cfg.copia(), "concurrente");
            sec[i] = s.tiempoTotalMs();
            con[i] = c.tiempoTotalMs();
            todasOk = todasOk && s.verificacionCorrecta() && c.verificacionCorrecta();
        }
        double promSec = promedio(sec);
        double promCon = promedio(con);
        return new ComparacionRendimiento(repeticiones, redondear(promSec), redondear(promCon),
                redondear(promSec / promCon), redondear((promSec - promCon) * 100 / promSec), sec, con, todasOk);
    }

    // pregunta de analisis: espacios = 0 y elementos = 5
    public List<ResultadoSimulacion> analisis() {
        List<ResultadoSimulacion> lista = new ArrayList<>();

        lista.add(simular(ConfiguracionSimulacion.porDefecto(), "concurrente", "Guía: espacios=5, elementos=0", null).sinEventos());

        ConfiguracionSimulacion sinEspacios = ConfiguracionSimulacion.porDefecto();
        sinEspacios.setEspaciosIniciales(0);
        lista.add(simular(sinEspacios, "concurrente", "espacios=0", null).sinEventos());

        ConfiguracionSimulacion cincoElementos = ConfiguracionSimulacion.porDefecto();
        cincoElementos.setElementosIniciales(5);
        lista.add(simular(cincoElementos, "concurrente", "elementos=5", null).sinEventos());

        return lista;
    }

    private static double promedio(long[] v) {
        long suma = 0;
        for (long x : v) suma += x;
        return (double) suma / v.length;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
