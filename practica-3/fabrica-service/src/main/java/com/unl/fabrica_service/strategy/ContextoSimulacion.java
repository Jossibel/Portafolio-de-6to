package com.unl.fabrica_service.strategy;

import com.unl.fabrica_service.dto.ConfiguracionSimulacion;
import com.unl.fabrica_service.model.Almacen;
import com.unl.fabrica_service.model.Pieza;
import com.unl.fabrica_service.model.TipoEvento;
import com.unl.fabrica_service.observer.PublicadorEventos;

import java.util.concurrent.atomic.AtomicInteger;

// datos compartidos de una ejecucion
public class ContextoSimulacion {

    private final ConfiguracionSimulacion config;
    private final Almacen almacen;
    private final PublicadorEventos eventos;
    private final AtomicInteger siguienteId = new AtomicInteger(1);
    private final AtomicInteger producidas = new AtomicInteger(0);
    private final AtomicInteger consumidas = new AtomicInteger(0);

    public ContextoSimulacion(ConfiguracionSimulacion config, Almacen almacen, PublicadorEventos eventos) {
        this.config = config;
        this.almacen = almacen;
        this.eventos = eventos;
    }

    // fabricar_pieza()
    public Pieza fabricarPieza(String productor) throws InterruptedException {
        Thread.sleep(config.getTiempoFabricacionMs());
        Pieza p = new Pieza(siguienteId.getAndIncrement(), productor);
        eventos.publicar(TipoEvento.FABRICA, productor, p.id(), almacen.ocupacion(), "Productor " + productor + " fabricó pieza " + p.id());
        return p;
    }

    // ensamblar(pieza)
    public void ensamblar(Pieza p, String consumidor) throws InterruptedException {
        Thread.sleep(config.getTiempoEnsambladoMs());
        eventos.publicar(TipoEvento.ENSAMBLA, consumidor, p.id(), almacen.ocupacion(), "Consumidor " + consumidor + " ensambló pieza " + p.id());
    }

    public ConfiguracionSimulacion getConfig() { return config; }
    public Almacen getAlmacen() { return almacen; }
    public AtomicInteger getProducidas() { return producidas; }
    public AtomicInteger getConsumidas() { return consumidas; }
}
