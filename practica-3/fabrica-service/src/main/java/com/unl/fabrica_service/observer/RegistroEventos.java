package com.unl.fabrica_service.observer;

import com.unl.fabrica_service.model.EventoSimulacion;

import java.util.ArrayList;
import java.util.List;

// guarda los eventos para devolverlos en la respuesta
public class RegistroEventos implements ObservadorSimulacion {

    private final List<EventoSimulacion> eventos = new ArrayList<>();

    @Override
    public synchronized void notificar(EventoSimulacion e) {
        eventos.add(e);
    }

    public synchronized List<EventoSimulacion> getEventos() {
        return new ArrayList<>(eventos);
    }
}
