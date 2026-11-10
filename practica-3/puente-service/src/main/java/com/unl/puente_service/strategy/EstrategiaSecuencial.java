package com.unl.puente_service.strategy;

import com.unl.puente_service.model.Vehiculo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// un solo hilo: los autos cruzan de uno en uno en orden de llegada
@Component
public class EstrategiaSecuencial implements EstrategiaEjecucion {

    @Override
    public String nombre() {
        return "secuencial";
    }

    @Override
    public void ejecutar(ContextoPuente ctx) throws InterruptedException {
        List<Vehiculo> orden = new ArrayList<>(ctx.getVehiculos());
        orden.sort(Comparator.comparingLong(Vehiculo::llegadaMs));
        for (Vehiculo v : orden) {
            ctx.cruzar(v);
        }
    }
}
