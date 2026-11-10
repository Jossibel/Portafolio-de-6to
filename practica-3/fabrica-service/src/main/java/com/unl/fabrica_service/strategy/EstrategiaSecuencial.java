package com.unl.fabrica_service.strategy;

import com.unl.fabrica_service.model.Almacen;
import com.unl.fabrica_service.model.Pieza;
import org.springframework.stereotype.Component;

// un solo hilo hace todo, pieza por pieza (mismo trabajo que la concurrente)
@Component
public class EstrategiaSecuencial implements EstrategiaEjecucion {

    @Override
    public String nombre() {
        return "secuencial";
    }

    @Override
    public void ejecutar(ContextoSimulacion ctx) throws InterruptedException {
        int piezas = ctx.getConfig().getPiezasPorProductor();
        int consumidores = ctx.getConfig().getConsumidores();
        int n = 0;

        for (int p = 1; p <= ctx.getConfig().getProductores(); p++) {
            for (int i = 0; i < piezas; i++) {
                String productor = "P" + p;
                String consumidor = "C" + (n % consumidores + 1); // turnos C1, C2, C1...
                n++;

                Pieza pieza = ctx.fabricarPieza(productor);
                if (ctx.getAlmacen().insertar(pieza, productor) == Almacen.Resultado.BLOQUEADO) return;
                ctx.getProducidas().incrementAndGet();

                Almacen.Extraccion ex = ctx.getAlmacen().extraer(consumidor);
                if (ex.resultado() == Almacen.Resultado.BLOQUEADO) return;
                if (ex.pieza() != null) {
                    ctx.getConsumidas().incrementAndGet();
                    ctx.ensamblar(ex.pieza(), consumidor);
                }
            }
        }
    }
}
