package com.unl.puente_service.observer;

import com.unl.puente_service.model.EventoPuente;

import java.util.ArrayList;
import java.util.List;

public class RegistroEventos implements ObservadorPuente {

    private final List<EventoPuente> eventos = new ArrayList<>();

    @Override
    public synchronized void notificar(EventoPuente e) {
        eventos.add(e);
    }

    public synchronized List<EventoPuente> getEventos() {
        return new ArrayList<>(eventos);
    }
}
