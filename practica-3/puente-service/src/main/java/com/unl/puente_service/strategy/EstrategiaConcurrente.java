package com.unl.puente_service.strategy;

import com.unl.puente_service.model.Vehiculo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// cada vehiculo es un hilo
@Component
public class EstrategiaConcurrente implements EstrategiaEjecucion {

    @Override
    public String nombre() {
        return "concurrente";
    }

    @Override
    public void ejecutar(ContextoPuente ctx) throws InterruptedException {
        List<Thread> hilos = new ArrayList<>();
        for (Vehiculo v : ctx.getVehiculos()) {
            hilos.add(new Thread(() -> {
                try {
                    ctx.cruzar(v);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "vehiculo-" + v.id()));
        }
        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join();
    }
}
