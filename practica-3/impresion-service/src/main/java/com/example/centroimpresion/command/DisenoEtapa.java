package com.example.centroimpresion.command;

import com.example.centroimpresion.controller.CentroImpresionController;
import com.example.centroimpresion.service.SincronizadorService;

public class DisenoEtapa implements ImpresionCommand {
    private final SincronizadorService sincronizador;
    private final long idSimulacion;

    public DisenoEtapa(SincronizadorService sincronizador, long idSimulacion) {
        this.sincronizador = sincronizador;
        this.idSimulacion = idSimulacion;
    }

    @Override
    public void ejecutarEtapa() throws InterruptedException {
        sincronizador.getTurnoDiseno().acquire();
        
        // CONTROL DE INTERRUPCIÓN: Si otra simulación inició, muere en silencio
        if (idSimulacion != CentroImpresionController.simulacionActivaId.get()) {
            sincronizador.getTurnoRevision().release(); // Liberar para no trabar el pipeline del nuevo hilo
            return;
        }

        CentroImpresionController.agregarLog("🎨 [DISEÑO] Preparando documento...");
        Thread.sleep(1500);
        sincronizador.getTurnoRevision().release();
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 3; i++) {
                if (idSimulacion != CentroImpresionController.simulacionActivaId.get()) break;
                ejecutarEtapa();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
