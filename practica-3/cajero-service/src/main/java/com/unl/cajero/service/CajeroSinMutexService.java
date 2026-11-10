
package com.unl.cajero.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class CajeroSinMutexService {

    private double saldo;

    public double simularSinMutex() throws InterruptedException {

        saldo = 1000;

        // Sincronizar el inicio de los tres hilos
        CountDownLatch inicio = new CountDownLatch(1);

        Thread proceso1 = new Thread(() -> {
            esperarInicio(inicio);
            retirar(300);
        });

        Thread proceso2 = new Thread(() -> {
            esperarInicio(inicio);
            retirar(250);
        });

        Thread proceso3 = new Thread(() -> {
            esperarInicio(inicio);
            depositar(200);
        });

        proceso1.start();
        proceso2.start();
        proceso3.start();

        inicio.countDown();

        proceso1.join();
        proceso2.join();
        proceso3.join();

        System.out.println("Saldo final SIN mutex: $" + saldo);

        return saldo;
    }

    private void retirar(double cantidad) {

        double saldoTemporal = saldo;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (saldoTemporal >= cantidad) {
            saldo = saldoTemporal - cantidad;
        }
    }

    private void depositar(double cantidad) {

        double saldoTemporal = saldo;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        saldo = saldoTemporal + cantidad;
    }

    private void esperarInicio(CountDownLatch inicio) {
        try {
            inicio.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
