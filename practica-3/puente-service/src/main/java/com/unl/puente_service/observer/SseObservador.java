package com.unl.puente_service.observer;

import com.unl.puente_service.model.EventoPuente;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

// manda cada evento al navegador en vivo (SSE)
public class SseObservador implements ObservadorPuente {

    private final SseEmitter emitter;
    private boolean activo = true;

    public SseObservador(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public synchronized void notificar(EventoPuente e) {
        if (!activo) return;
        try {
            emitter.send(SseEmitter.event().name("evento").data(e));
        } catch (IOException | IllegalStateException ex) {
            activo = false;
        }
    }
}
