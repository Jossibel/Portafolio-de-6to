package com.unl.puente_service.observer;

import com.unl.puente_service.model.EventoPuente;

// patron Observer
public interface ObservadorPuente {
    void notificar(EventoPuente evento);
}
