package com.unl.fabrica_service.model;

import com.unl.fabrica_service.observer.PublicadorEventos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

// Almacen compartido (buffer) del productor-consumidor
public class Almacen {

    public enum Resultado { OK, BLOQUEADO, INCONSISTENTE }

    public record Extraccion(Resultado resultado, Pieza pieza) {}

    private final int capacidad;
    private final Deque<Pieza> cola = new ArrayDeque<>();
    private final Semaphore espacios;   // huecos libres
    private final Semaphore elementos;  // piezas disponibles
    private final ReentrantLock mutex = new ReentrantLock();
    private final long timeoutMs;
    private final PublicadorEventos eventos;

    private int ocupacionMaxima = 0;
    private int inconsistencias = 0;
    private int bloqueos = 0;

    public Almacen(int capacidad, int espacios, int elementos, long timeoutMs, PublicadorEventos eventos) {
        this.capacidad = capacidad;
        this.espacios = new Semaphore(espacios);
        this.elementos = new Semaphore(elementos);
        this.timeoutMs = timeoutMs;
        this.eventos = eventos;
    }

    public Resultado insertar(Pieza pieza, String actor) throws InterruptedException {
        // wait(espacios)
        // uso tryAcquire con tiempo limite para detectar el bloqueo (si no, el hilo se queda colgado para siempre)
        if (!espacios.tryAcquire()) {
            eventos.publicar(TipoEvento.ESPERA_ESPACIO, actor, pieza.id(), ocupacion(), actor + " espera: almacén lleno");
            if (!espacios.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                sumarBloqueo();
                eventos.publicar(TipoEvento.BLOQUEO, actor, pieza.id(), ocupacion(), actor + " bloqueado, nunca hubo espacio");
                return Resultado.BLOQUEADO;
            }
        }

        Resultado r = Resultado.OK;
        mutex.lock();
        try {
            cola.addLast(pieza);
            ocupacionMaxima = Math.max(ocupacionMaxima, cola.size());
            eventos.publicar(TipoEvento.INSERTA, actor, pieza.id(), cola.size(), "Productor " + actor + " agregó pieza " + pieza.id());
            if (cola.size() > capacidad) {
                inconsistencias++;
                r = Resultado.INCONSISTENTE;
                eventos.publicar(TipoEvento.INCONSISTENCIA, actor, pieza.id(), cola.size(), "El almacén pasó la capacidad: " + cola.size());
            }
        } finally {
            mutex.unlock();
        }

        elementos.release(); // signal(elementos)
        return r;
    }

    public Extraccion extraer(String actor) throws InterruptedException {
        // wait(elementos)
        if (!elementos.tryAcquire()) {
            eventos.publicar(TipoEvento.ESPERA_PIEZA, actor, null, ocupacion(), actor + " espera: almacén vacío");
            if (!elementos.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                sumarBloqueo();
                eventos.publicar(TipoEvento.BLOQUEO, actor, null, ocupacion(), actor + " bloqueado, nunca llegó una pieza");
                return new Extraccion(Resultado.BLOQUEADO, null);
            }
        }

        Extraccion ex;
        mutex.lock();
        try {
            Pieza pieza = cola.pollFirst();
            if (pieza == null) {
                // pasa cuando elementos empieza en 5: el semaforo dice que hay piezas pero la cola esta vacia
                inconsistencias++;
                eventos.publicar(TipoEvento.INCONSISTENCIA, actor, null, 0, "Consumidor " + actor + " intentó sacar de un almacén vacío");
                ex = new Extraccion(Resultado.INCONSISTENTE, null);
            } else {
                eventos.publicar(TipoEvento.EXTRAE, actor, pieza.id(), cola.size(), "Consumidor " + actor + " retiró pieza " + pieza.id());
                ex = new Extraccion(Resultado.OK, pieza);
            }
        } finally {
            mutex.unlock();
        }

        espacios.release(); // signal(espacios)
        return ex;
    }

    public int ocupacion() {
        mutex.lock();
        try {
            return cola.size();
        } finally {
            mutex.unlock();
        }
    }

    private void sumarBloqueo() {
        mutex.lock();
        try {
            bloqueos++;
        } finally {
            mutex.unlock();
        }
    }

    public int getOcupacionMaxima() { mutex.lock(); try { return ocupacionMaxima; } finally { mutex.unlock(); } }
    public int getInconsistencias() { mutex.lock(); try { return inconsistencias; } finally { mutex.unlock(); } }
    public int getBloqueos() { mutex.lock(); try { return bloqueos; } finally { mutex.unlock(); } }
}
