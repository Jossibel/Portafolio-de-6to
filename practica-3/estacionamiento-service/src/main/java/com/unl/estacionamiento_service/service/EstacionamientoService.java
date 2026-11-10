package com.unl.estacionamiento_service.service;

import com.unl.estacionamiento_service.dto.EstadoEstacionamientoResponse;
import com.unl.estacionamiento_service.model.Vehiculo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
public class EstacionamientoService {

    private final Semaphore espacios = new Semaphore(4);
    private final List<Vehiculo> vehiculos = new ArrayList<>();

    public void procesarVehiculo(int id) {

        Vehiculo vehiculo = new Vehiculo(id, "ESPERANDO");

        synchronized (vehiculos) {
            vehiculos.add(vehiculo);
        }

        System.out.println("Vehículo " + id + " llega al estacionamiento");

        try {

            espacios.acquire();

            vehiculo.setEstado("DENTRO");

            System.out.println("Vehículo " + id + " ingresa al estacionamiento");

            Thread.sleep(3000);

            vehiculo.setEstado("FUERA");

            System.out.println("Vehículo " + id + " sale del estacionamiento");

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

        } finally {

            espacios.release();
        }
    }

    public List<Vehiculo> obtenerVehiculos() {
        return vehiculos;
    }

    public EstadoEstacionamientoResponse obtenerEstado() {

    int ocupados = 0;
    int esperando = 0;

    synchronized (vehiculos) {
        for (Vehiculo vehiculo : vehiculos) {

            if ("DENTRO".equals(vehiculo.getEstado())) {
                ocupados++;
            }

            if ("ESPERANDO".equals(vehiculo.getEstado())) {
                esperando++;
            }
        }
    }

    int capacidad = 4;
    int disponibles = capacidad - ocupados;

    return new EstadoEstacionamientoResponse(
            capacidad,
            ocupados,
            disponibles,
            esperando
    );
}
}