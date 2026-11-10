package com.unl.puente_service.controller;

import com.unl.puente_service.dto.AnalisisInanicion;
import com.unl.puente_service.dto.ComparacionRendimiento;
import com.unl.puente_service.dto.ConfiguracionPuente;
import com.unl.puente_service.dto.ResultadoPuente;
import com.unl.puente_service.observer.SseObservador;
import com.unl.puente_service.service.PuenteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/puente")
public class PuenteController {

    private final PuenteService servicio;

    public PuenteController(PuenteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("servicio", "puente-service", "estado", "UP");
    }

    // politica: base (la de la guia) o justa (el reto)
    @PostMapping("/simular")
    public ResultadoPuente simular(@RequestParam(defaultValue = "concurrente") String modo,
                                   @RequestParam(defaultValue = "base") String politica,
                                   @Valid @RequestBody(required = false) ConfiguracionPuente cfg) {
        if (cfg == null) cfg = ConfiguracionPuente.porDefecto();
        return servicio.simular(cfg, modo, politica);
    }

    @GetMapping("/inanicion")
    public AnalisisInanicion inanicion() {
        return servicio.analisisInanicion();
    }

    @GetMapping("/comparar")
    public ComparacionRendimiento comparar(@RequestParam(defaultValue = "5") int repeticiones) {
        repeticiones = Math.max(1, Math.min(repeticiones, 20));
        return servicio.comparar(ConfiguracionPuente.porDefecto(), repeticiones);
    }

    // simulacion en vivo para el frontend: se hace mas lenta para que se vea la animacion
    @GetMapping(path = "/stream", produces = "text/event-stream")
    public SseEmitter stream(@RequestParam(defaultValue = "base") String politica,
                             @RequestParam(defaultValue = "mixto") String escenario,
                             @RequestParam(defaultValue = "900") long cruceMs) {
        boolean inanicion = escenario.equals("inanicion");
        ConfiguracionPuente cfg = inanicion ? ConfiguracionPuente.escenarioInanicion() : ConfiguracionPuente.porDefecto();
        long cruceOriginal = cfg.getTiempoCruceMs();
        cfg.setTiempoCruceMs(Math.max(100, Math.min(cruceMs, 5000)));
        // las llegadas se estiran en la misma proporcion que el cruce
        cfg.setIntervaloLlegadaMs(cfg.getIntervaloLlegadaMs() * cfg.getTiempoCruceMs() / cruceOriginal);

        SseEmitter emitter = new SseEmitter(180_000L);
        new Thread(() -> {
            try {
                ResultadoPuente r = servicio.simular(cfg, "concurrente", politica, new SseObservador(emitter));
                emitter.send(SseEmitter.event().name("resultado").data(r.sinEventos()));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> error(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
