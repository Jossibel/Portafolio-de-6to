package com.example.centroimpresion.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.Semaphore;

@Service
public class SincronizadorService {
    // Semáforos inicializados según la guía práctica
    private Semaphore turnoDiseno = new Semaphore(1);
    private  Semaphore turnoRevision = new Semaphore(0);
    private  Semaphore turnoImpresion = new Semaphore(0);

    public Semaphore getTurnoDiseno() { return turnoDiseno; }
    public Semaphore getTurnoRevision() { return turnoRevision; }
    public Semaphore getTurnoImpresion() { return turnoImpresion; }

    public void reiniciarSemaforos() {
        this.turnoDiseno = new Semaphore(1);
        this.turnoRevision = new Semaphore(0);
        this.turnoImpresion = new Semaphore(0);
    }
}