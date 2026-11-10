package com.unl.puente_service.model;

import com.unl.puente_service.observer.PublicadorEventos;
import com.unl.puente_service.policy.PoliticaAcceso;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// El puente es un monitor: variables compartidas + mutex + variable de condicion.
// En el pseudocodigo de la guia el "esperar()" esta dentro del lock(mutex). Si se espera
// sin soltar el mutex nadie puede salir del puente y se congela todo. Por eso uso
// condicion.await(), que suelta el mutex mientras espera y lo vuelve a tomar al despertar.
public class Puente {

    private final ReentrantLock mutex = new ReentrantLock();
    private final Condition condicion = mutex.newCondition();
    private final PoliticaAcceso politica;
    private final PublicadorEventos eventos;

    // variables del pseudocodigo
    private Direccion direccion = Direccion.NINGUNA;
    private int vehiculosNorte = 0;
    private int vehiculosSur = 0;

    // para el reto (politica justa)
    private int esperandoNorte = 0;
    private int esperandoSur = 0;
    private int consecutivos = 0;
    private Direccion turno = Direccion.NINGUNA;

    // para verificar
    private int violaciones = 0;
    private int maxSimultaneos = 0;
    private int cambiosDireccion = 0;

    public Puente(PoliticaAcceso politica, PublicadorEventos eventos) {
        this.politica = politica;
        this.eventos = eventos;
    }

    // primera parte de vehiculo_norte(id) / vehiculo_sur(id). Devuelve cuanto espero (ms)
    public long entrar(Vehiculo v) throws InterruptedException {
        Direccion d = v.direccion();
        long inicio = System.nanoTime();

        mutex.lock();
        try {
            cambiarEsperando(d, +1);
            boolean yaAviso = false;
            while (!politica.puedeEntrar(d, this)) { // MIENTRAS direccion == contraria HACER esperar()
                if (!yaAviso) {
                    publicar(TipoEvento.ESPERA, v.id(), "Vehículo " + v.id() + " espera para ir al " + d);
                    yaAviso = true;
                }
                condicion.await();
            }
            cambiarEsperando(d, -1);

            if (direccion != d) {
                direccion = d;
                consecutivos = 0;
                cambiosDireccion++;
                publicar(TipoEvento.CAMBIO_DIRECCION, v.id(), "El puente cambia a dirección " + d);
            }
            consecutivos++;
            cambiarEnPuente(d, +1);

            // criterio de verificacion: nunca debe haber autos en los dos sentidos
            if (enPuente(d.contraria()) > 0) {
                violaciones++;
                publicar(TipoEvento.VIOLACION, v.id(), "ERROR: vehículos en sentidos opuestos");
            }
            maxSimultaneos = Math.max(maxSimultaneos, enPuente(d));
            publicar(TipoEvento.ENTRA, v.id(), "Vehículo " + v.id() + " cruza hacia el " + d);
        } finally {
            mutex.unlock();
        }
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    // segunda parte: restar el contador y liberar el puente si queda vacio
    public void salir(Vehiculo v) {
        Direccion d = v.direccion();
        mutex.lock();
        try {
            cambiarEnPuente(d, -1);
            publicar(TipoEvento.SALE, v.id(), "Vehículo " + v.id() + " salió del puente");
            if (enPuente(d) == 0) {
                politica.alQuedarLibre(d, this);
                direccion = Direccion.NINGUNA;
                publicar(TipoEvento.CAMBIO_DIRECCION, v.id(), "Puente libre");
            }
            condicion.signalAll(); // despertar a los que esperan para que vuelvan a revisar
        } finally {
            mutex.unlock();
        }
    }

    private void cambiarEnPuente(Direccion d, int n) {
        if (d == Direccion.NORTE) vehiculosNorte += n; else vehiculosSur += n;
    }

    private void cambiarEsperando(Direccion d, int n) {
        if (d == Direccion.NORTE) esperandoNorte += n; else esperandoSur += n;
    }

    private void publicar(TipoEvento tipo, String vehiculo, String mensaje) {
        eventos.publicar(tipo, vehiculo, direccion, vehiculosNorte, vehiculosSur, mensaje);
    }

    // estos los usa la politica (siempre con el mutex tomado)
    public Direccion getDireccion() { return direccion; }
    public int getConsecutivos() { return consecutivos; }
    public Direccion getTurno() { return turno; }
    public void setTurno(Direccion t) { turno = t; }

    public int enPuente(Direccion d) {
        return d == Direccion.NORTE ? vehiculosNorte : d == Direccion.SUR ? vehiculosSur : 0;
    }

    public int esperando(Direccion d) {
        return d == Direccion.NORTE ? esperandoNorte : d == Direccion.SUR ? esperandoSur : 0;
    }

    public int getViolaciones() { mutex.lock(); try { return violaciones; } finally { mutex.unlock(); } }
    public int getMaxSimultaneos() { mutex.lock(); try { return maxSimultaneos; } finally { mutex.unlock(); } }
    public int getCambiosDireccion() { mutex.lock(); try { return cambiosDireccion; } finally { mutex.unlock(); } }
}
