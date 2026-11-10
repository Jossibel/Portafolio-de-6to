package com.unl.fabrica_service.observer;

import com.unl.fabrica_service.model.EventoSimulacion;
import com.unl.fabrica_service.model.TipoEvento;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// los hilos publican aqui y se avisa a todos los observadores
public class PublicadorEventos {

    private final List<ObservadorSimulacion> observadores = new CopyOnWriteArrayList<>();
    private final long inicio = System.nanoTime();

    public void suscribir(ObservadorSimulacion o) {
        observadores.add(o);
    }

    public void publicar(TipoEvento tipo, String actor, Integer piezaId, int ocupacion, String mensaje) {
        long tMs = (System.nanoTime() - inicio) / 1_000_000;
        EventoSimulacion e = new EventoSimulacion(tMs, tipo, actor, piezaId, ocupacion, Thread.currentThread().getName(), mensaje);
        for (ObservadorSimulacion o : observadores) {
            o.notificar(e);
        }
    }
}
