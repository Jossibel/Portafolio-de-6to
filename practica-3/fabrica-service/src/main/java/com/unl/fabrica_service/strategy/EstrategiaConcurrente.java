package com.unl.fabrica_service.strategy;

import com.unl.fabrica_service.model.Almacen;
import com.unl.fabrica_service.model.Pieza;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// cada productor y cada consumidor es un hilo
@Component
public class EstrategiaConcurrente implements EstrategiaEjecucion {

    @Override
    public String nombre() {
        return "concurrente";
    }

    @Override
    public void ejecutar(ContextoSimulacion ctx) throws InterruptedException {
        List<Thread> hilos = new ArrayList<>();
        int productores = ctx.getConfig().getProductores();
        int consumidores = ctx.getConfig().getConsumidores();
        int total = ctx.getConfig().totalPiezas();

        for (int i = 1; i <= productores; i++) {
            String id = "P" + i;
            hilos.add(new Thread(() -> productor(ctx, id), "productor-" + id));
        }
        for (int i = 1; i <= consumidores; i++) {
            String id = "C" + i;
            // se reparten las piezas entre los consumidores (con la guia: 5 cada uno)
            int cuota = total / consumidores + (i <= total % consumidores ? 1 : 0);
            hilos.add(new Thread(() -> consumidor(ctx, id, cuota), "consumidor-" + id));
        }

        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join(); // esperar a que todos terminen
    }

    private void productor(ContextoSimulacion ctx, String id) {
        try {
            for (int i = 0; i < ctx.getConfig().getPiezasPorProductor(); i++) {
                Pieza pieza = ctx.fabricarPieza(id);
                if (ctx.getAlmacen().insertar(pieza, id) == Almacen.Resultado.BLOQUEADO) return;
                ctx.getProducidas().incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void consumidor(ContextoSimulacion ctx, String id, int cuota) {
        try {
            for (int i = 0; i < cuota; i++) {
                Almacen.Extraccion ex = ctx.getAlmacen().extraer(id);
                if (ex.resultado() == Almacen.Resultado.BLOQUEADO) return;
                if (ex.pieza() != null) {
                    ctx.getConsumidas().incrementAndGet();
                    ctx.ensamblar(ex.pieza(), id);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
