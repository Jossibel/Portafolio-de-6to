package com.unl.puente_service.service;

import com.unl.puente_service.dto.AnalisisInanicion;
import com.unl.puente_service.dto.ComparacionRendimiento;
import com.unl.puente_service.dto.ConfiguracionPuente;
import com.unl.puente_service.dto.ResultadoPuente;
import com.unl.puente_service.model.Direccion;
import com.unl.puente_service.model.Puente;
import com.unl.puente_service.model.TipoEvento;
import com.unl.puente_service.model.Vehiculo;
import com.unl.puente_service.observer.ObservadorPuente;
import com.unl.puente_service.observer.PublicadorEventos;
import com.unl.puente_service.observer.RegistroEventos;
import com.unl.puente_service.policy.PoliticaAcceso;
import com.unl.puente_service.policy.PoliticaBase;
import com.unl.puente_service.policy.PoliticaJusta;
import com.unl.puente_service.strategy.ContextoPuente;
import com.unl.puente_service.strategy.EstrategiaEjecucion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PuenteService {

    private static final Logger log = LoggerFactory.getLogger("puente");

    private final Map<String, EstrategiaEjecucion> estrategias = new HashMap<>();

    public PuenteService(List<EstrategiaEjecucion> lista) {
        for (EstrategiaEjecucion e : lista) {
            estrategias.put(e.nombre(), e);
        }
    }

    public ResultadoPuente simular(ConfiguracionPuente cfg, String modo, String politica) {
        return simular(cfg, modo, politica, null);
    }

    public ResultadoPuente simular(ConfiguracionPuente cfg, String modo, String nombrePolitica, ObservadorPuente extra) {
        EstrategiaEjecucion estrategia = estrategias.get(modo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Modo no válido: " + modo + " (use concurrente o secuencial)");
        }
        PoliticaAcceso politica = crearPolitica(nombrePolitica, cfg.getLimiteConsecutivos());

        PublicadorEventos eventos = new PublicadorEventos();
        RegistroEventos registro = new RegistroEventos();
        eventos.suscribir(registro);
        eventos.suscribir(e -> log.info("[{} ms] [{}] {} | dir={} norte={} sur={}", e.tMs(), e.hilo(), e.mensaje(),
                e.direccion(), e.enNorte(), e.enSur()));
        if (extra != null) eventos.suscribir(extra);

        Puente puente = new Puente(politica, eventos);
        ContextoPuente ctx = new ContextoPuente(cfg, puente, eventos, crearVehiculos(cfg));

        eventos.publicar(TipoEvento.INICIO, "SISTEMA", Direccion.NINGUNA, 0, 0, "Inicio " + modo + " | política="
                + politica.nombre() + " | norte=" + cfg.getVehiculosNorte() + " sur=" + cfg.getVehiculosSur());

        long inicio = System.nanoTime();
        try {
            estrategia.ejecutar(ctx);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulación interrumpida");
        }
        long tiempoMs = (System.nanoTime() - inicio) / 1_000_000;

        int norte = ctx.getEsperasNorte().size();
        int sur = ctx.getEsperasSur().size();
        boolean unaDireccion = puente.getViolaciones() == 0;
        boolean todos = norte == cfg.getVehiculosNorte() && sur == cfg.getVehiculosSur();

        eventos.publicar(TipoEvento.FIN, "SISTEMA", Direccion.NINGUNA, 0, 0,
                "Fin en " + tiempoMs + " ms | violaciones=" + puente.getViolaciones());

        return new ResultadoPuente(cfg.getEscenario(), modo, politica.nombre(), cfg, cfg.total(), norte, sur,
                puente.getViolaciones(), puente.getMaxSimultaneos(), puente.getCambiosDireccion(),
                maximo(ctx.getEsperasNorte()), maximo(ctx.getEsperasSur()),
                promedio(ctx.getEsperasNorte()), promedio(ctx.getEsperasSur()), tiempoMs,
                unaDireccion, todos, unaDireccion && todos, registro.getEventos());
    }

    // analisis de inanicion: mismo escenario con las dos politicas
    public AnalisisInanicion analisisInanicion() {
        ResultadoPuente base = simular(ConfiguracionPuente.escenarioInanicion(), "concurrente", "base").sinEventos();
        ResultadoPuente justa = simular(ConfiguracionPuente.escenarioInanicion(), "concurrente", "justa").sinEventos();
        double reduccion = 0;
        if (base.esperaMaxSurMs() > 0) {
            reduccion = redondear((base.esperaMaxSurMs() - justa.esperaMaxSurMs()) * 100.0 / base.esperaMaxSurMs());
        }
        return new AnalisisInanicion(base, justa, reduccion);
    }

    // secuencial (de uno en uno) vs concurrente (varios del mismo sentido a la vez)
    public ComparacionRendimiento comparar(ConfiguracionPuente cfg, int repeticiones) {
        long[] sec = new long[repeticiones];
        long[] con = new long[repeticiones];
        boolean todasOk = true;
        for (int i = 0; i < repeticiones; i++) {
            ResultadoPuente s = simular(cfg.copia(), "secuencial", "base");
            ResultadoPuente c = simular(cfg.copia(), "concurrente", "base");
            sec[i] = s.tiempoTotalMs();
            con[i] = c.tiempoTotalMs();
            todasOk = todasOk && s.verificacionCorrecta() && c.verificacionCorrecta();
        }
        double ps = promedio(sec);
        double pc = promedio(con);
        return new ComparacionRendimiento(repeticiones, redondear(ps), redondear(pc), redondear(ps / pc),
                redondear((ps - pc) * 100 / ps), sec, con, todasOk);
    }

    private PoliticaAcceso crearPolitica(String nombre, int limite) {
        if (nombre.equals("base")) return new PoliticaBase();
        if (nombre.equals("justa")) return new PoliticaJusta(limite);
        throw new IllegalArgumentException("Política no válida: " + nombre + " (use base o justa)");
    }

    // horario de llegada de los autos (fijo para que las pruebas se puedan repetir)
    private List<Vehiculo> crearVehiculos(ConfiguracionPuente cfg) {
        List<Vehiculo> lista = new ArrayList<>();
        long intervalo = cfg.getIntervaloLlegadaMs();
        boolean inanicion = cfg.getEscenario().equals("inanicion");

        for (int i = 0; i < cfg.getVehiculosNorte(); i++) {
            lista.add(new Vehiculo("N" + (i + 1), Direccion.NORTE, i * intervalo));
        }
        for (int i = 0; i < cfg.getVehiculosSur(); i++) {
            // inanicion: los del sur llegan cuando el norte ya esta usando el puente
            long llegada = inanicion ? 100 + i * 50L : i * intervalo + intervalo / 2;
            lista.add(new Vehiculo("S" + (i + 1), Direccion.SUR, llegada));
        }
        return lista;
    }

    private static long maximo(Collection<Long> v) {
        long max = 0;
        for (long x : v) max = Math.max(max, x);
        return max;
    }

    private static double promedio(Collection<Long> v) {
        if (v.isEmpty()) return 0;
        long suma = 0;
        for (long x : v) suma += x;
        return redondear((double) suma / v.size());
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
