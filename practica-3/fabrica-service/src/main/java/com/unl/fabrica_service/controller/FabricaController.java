package com.unl.fabrica_service.controller;

import com.unl.fabrica_service.dto.ComparacionRendimiento;
import com.unl.fabrica_service.dto.ConfiguracionSimulacion;
import com.unl.fabrica_service.dto.ResultadoSimulacion;
import com.unl.fabrica_service.observer.SseObservador;
import com.unl.fabrica_service.service.SimulacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fabrica")
public class FabricaController {

    private final SimulacionService servicio;

    public FabricaController(SimulacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("servicio", "fabrica-service", "estado", "UP");
    }

    @PostMapping("/simular")
    public ResultadoSimulacion simular(@RequestParam(defaultValue = "concurrente") String modo,
                                       @Valid @RequestBody(required = false) ConfiguracionSimulacion cfg) {
        if (cfg == null) cfg = ConfiguracionSimulacion.porDefecto();
        return servicio.simular(cfg, modo);
    }

    @GetMapping("/comparar")
    public ComparacionRendimiento comparar(@RequestParam(defaultValue = "5") int repeticiones) {
        repeticiones = Math.max(1, Math.min(repeticiones, 20));
        return servicio.comparar(ConfiguracionSimulacion.porDefecto(), repeticiones);
    }

    @GetMapping("/analisis")
    public List<ResultadoSimulacion> analisis() {
        return servicio.analisis();
    }

    // simulacion en vivo para el frontend (tiempos mas lentos para que se vea)
    @GetMapping(path = "/stream", produces = "text/event-stream")
    public SseEmitter stream(@RequestParam(defaultValue = "concurrente") String modo,
                             @RequestParam(defaultValue = "5") int espacios,
                             @RequestParam(defaultValue = "0") int elementos) {
        ConfiguracionSimulacion cfg = ConfiguracionSimulacion.porDefecto();
        cfg.setEspaciosIniciales(Math.max(0, Math.min(espacios, 50)));
        cfg.setElementosIniciales(Math.max(0, Math.min(elementos, 50)));
        cfg.setTiempoFabricacionMs(400);
        cfg.setTiempoEnsambladoMs(600);

        SseEmitter emitter = new SseEmitter(120_000L);
        new Thread(() -> {
            try {
                ResultadoSimulacion r = servicio.simular(cfg, modo, "en vivo", new SseObservador(emitter));
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
