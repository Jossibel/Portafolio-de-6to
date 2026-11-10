package com.unl.fabrica_service.observer;

import com.unl.fabrica_service.model.EventoSimulacion;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

// manda cada evento al navegador en vivo (SSE)
public class SseObservador implements ObservadorSimulacion {

    private final SseEmitter emitter;
    private boolean activo = true;

    public SseObservador(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public synchronized void notificar(EventoSimulacion e) {
        if (!activo) return;
        try {
            emitter.send(SseEmitter.event().name("evento").data(e));
        } catch (IOException | IllegalStateException ex) {
            activo = false; // se cerro el navegador
        }
    }
}
