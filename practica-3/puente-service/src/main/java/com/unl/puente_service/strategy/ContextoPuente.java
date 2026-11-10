package com.unl.puente_service.strategy;

import com.unl.puente_service.dto.ConfiguracionPuente;
import com.unl.puente_service.model.Direccion;
import com.unl.puente_service.model.Puente;
import com.unl.puente_service.model.TipoEvento;
import com.unl.puente_service.model.Vehiculo;
import com.unl.puente_service.observer.PublicadorEventos;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ContextoPuente {

    private final ConfiguracionPuente config;
    private final Puente puente;
    private final PublicadorEventos eventos;
    private final List<Vehiculo> vehiculos;
    private final Queue<Long> esperasNorte = new ConcurrentLinkedQueue<>();
    private final Queue<Long> esperasSur = new ConcurrentLinkedQueue<>();

    public ContextoPuente(ConfiguracionPuente config, Puente puente, PublicadorEventos eventos, List<Vehiculo> vehiculos) {
        this.config = config;
        this.puente = puente;
        this.eventos = eventos;
        this.vehiculos = vehiculos;
    }

    // lo que hace un vehiculo: llegar, entrar (puede esperar), cruzar y salir
    public void cruzar(Vehiculo v) throws InterruptedException {
        long falta = v.llegadaMs() - eventos.transcurridoMs();
        if (falta > 0) Thread.sleep(falta);
        eventos.publicar(TipoEvento.LLEGA, v.id(), null, 0, 0, "Vehículo " + v.id() + " llega (va al " + v.direccion() + ")");

        long espera = puente.entrar(v);
        if (v.direccion() == Direccion.NORTE) esperasNorte.add(espera); else esperasSur.add(espera);

        Thread.sleep(config.getTiempoCruceMs()); // cruzar_puente(id)
        puente.salir(v);
    }

    public ConfiguracionPuente getConfig() { return config; }
    public Puente getPuente() { return puente; }
    public List<Vehiculo> getVehiculos() { return vehiculos; }
    public Queue<Long> getEsperasNorte() { return esperasNorte; }
    public Queue<Long> getEsperasSur() { return esperasSur; }
}
