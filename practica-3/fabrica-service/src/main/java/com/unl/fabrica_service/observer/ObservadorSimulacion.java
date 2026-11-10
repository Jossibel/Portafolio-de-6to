package com.unl.fabrica_service.observer;

import com.unl.fabrica_service.model.EventoSimulacion;

// patron Observer
public interface ObservadorSimulacion {
    void notificar(EventoSimulacion evento);
}
