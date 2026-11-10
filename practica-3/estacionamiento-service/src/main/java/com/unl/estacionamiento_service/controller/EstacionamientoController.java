package com.unl.estacionamiento_service.controller;

import com.unl.estacionamiento_service.model.Vehiculo;
import com.unl.estacionamiento_service.service.EstacionamientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.unl.estacionamiento_service.dto.EstadoEstacionamientoResponse;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/estacionamiento")
public class EstacionamientoController {

    private final EstacionamientoService estacionamientoService;

    public EstacionamientoController(EstacionamientoService estacionamientoService) {
        this.estacionamientoService = estacionamientoService;
    }

    @PostMapping("/simular")
    public String simularEstacionamiento() {

        for (int i = 1; i <= 10; i++) {

            int id = i;

            Thread hilo = new Thread(() ->
                    estacionamientoService.procesarVehiculo(id)
            );

            hilo.start();
        }

        return "Simulación iniciada con 10 vehículos";
    }

    @GetMapping("/vehiculos")
    public List<Vehiculo> obtenerVehiculos() {
        return estacionamientoService.obtenerVehiculos();
    }

    @GetMapping("/estado")
    public EstadoEstacionamientoResponse obtenerEstado() {
        return estacionamientoService.obtenerEstado();
    }

}   