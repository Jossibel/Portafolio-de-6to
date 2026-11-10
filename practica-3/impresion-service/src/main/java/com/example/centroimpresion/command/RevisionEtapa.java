package com.example.centroimpresion.command;

import com.example.centroimpresion.controller.CentroImpresionController;
import com.example.centroimpresion.service.SincronizadorService;

public class RevisionEtapa implements ImpresionCommand {
    private final SincronizadorService sincronizador;
    private final long idSimulacion;

    public RevisionEtapa(SincronizadorService sincronizador, long idSimulacion) {
        this.sincronizador = sincronizador;
        this.idSimulacion = idSimulacion;
    }

    @Override
    public void ejecutarEtapa() throws InterruptedException {
        sincronizador.getTurnoRevision().acquire();
        
        if (idSimulacion != CentroImpresionController.simulacionActivaId.get()) {
            sincronizador.getTurnoImpresion().release();
            return;
        }

        CentroImpresionController.agregarLog("🔍 [REVISIÓN] Verificando documento...");
        Thread.sleep(1500);
        sincronizador.getTurnoImpresion().release();
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
