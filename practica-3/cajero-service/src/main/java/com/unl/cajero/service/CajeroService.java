
package com.unl.cajero.service;

import com.unl.cajero.model.Operacion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Service
public class CajeroService {

    // Recurso compartido
    private double saldo = 1000;

    // Mutex que protege el saldo
    private final ReentrantLock mutexSaldo = new ReentrantLock();

    // Historial de operaciones para el frontend
    private final List<Operacion> operaciones = new ArrayList<>();

    public void retirar(int id, double cantidad) {

        Operacion operacion = new Operacion(id, "RETIRO", cantidad);
        registrarOperacion(operacion);

        operacion.setEstado("ESPERANDO");

        mutexSaldo.lock();

        try {
            operacion.setEstado("EJECUTANDO");

            System.out.println(
                "Proceso " + id + " intenta retirar $" + cantidad
            );

            if (saldo >= cantidad) {

                saldo = saldo - cantidad;

                System.out.println("Retiro realizado");
                System.out.println("Saldo actual: $" + saldo);

                operacion.setEstado("COMPLETADA");

            } else {

                System.out.println("Saldo insuficiente");

                operacion.setEstado("RECHAZADA");
            }

        } finally {
            mutexSaldo.unlock();
        }
    }

    public void depositar(int id, double cantidad) {

        Operacion operacion = new Operacion(id, "DEPOSITO", cantidad);
        registrarOperacion(operacion);

        operacion.setEstado("ESPERANDO");

        mutexSaldo.lock();

        try {
            operacion.setEstado("EJECUTANDO");

            System.out.println(
                "Proceso " + id + " deposita $" + cantidad
            );

            saldo = saldo + cantidad;

            System.out.println("Saldo actual: $" + saldo);

            operacion.setEstado("COMPLETADA");

        } finally {
            mutexSaldo.unlock();
        }
    }

    private void registrarOperacion(Operacion operacion) {
        synchronized (operaciones) {
            operaciones.add(operacion);
        }
    }

    public double obtenerSaldo() {
        mutexSaldo.lock();

        try {
            return saldo;
        } finally {
            mutexSaldo.unlock();
        }
    }

    public List<Operacion> obtenerOperaciones() {
        synchronized (operaciones) {
            return new ArrayList<>(operaciones);
        }
    }



    public synchronized double simular() {

    // Reiniciar los datos del ejercicio
    mutexSaldo.lock();

    try {
        saldo = 1000;
    } finally {
        mutexSaldo.unlock();
    }

    synchronized (operaciones) {
        operaciones.clear();
    }

    // Crear tres hilos concurrentes
    ExecutorService executor = Executors.newFixedThreadPool(3);

    try {

        Future<?> proceso1 = executor.submit(
            () -> retirar(1, 300)
        );

        Future<?> proceso2 = executor.submit(
            () -> retirar(2, 250)
        );

        Future<?> proceso3 = executor.submit(
            () -> depositar(3, 200)
        );

        // Esperar a que terminen los tres procesos
        proceso1.get();
        proceso2.get();
        proceso3.get();

        return obtenerSaldo();

    } catch (Exception e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(
            "No se pudo completar la simulación", e
        );

    } finally {
        executor.shutdown();
    }
    }

}
