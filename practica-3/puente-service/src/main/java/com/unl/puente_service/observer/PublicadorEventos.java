package com.unl.puente_service.observer;

import com.unl.puente_service.model.Direccion;
import com.unl.puente_service.model.EventoPuente;
import com.unl.puente_service.model.TipoEvento;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PublicadorEventos {

    private final List<ObservadorPuente> observadores = new CopyOnWriteArrayList<>();
    private final long inicio = System.nanoTime();

    public void suscribir(ObservadorPuente o) {
        observadores.add(o);
    }

    public long transcurridoMs() {
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    public void publicar(TipoEvento tipo, String vehiculo, Direccion dir, int enNorte, int enSur, String mensaje) {
        EventoPuente e = new EventoPuente(transcurridoMs(), tipo, vehiculo, dir, enNorte, enSur,
                Thread.currentThread().getName(), mensaje);
        for (ObservadorPuente o : observadores) {
            o.notificar(e);
        }
    }
}
