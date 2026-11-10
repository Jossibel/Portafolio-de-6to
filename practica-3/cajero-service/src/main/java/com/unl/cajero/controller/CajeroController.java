
package com.unl.cajero.controller;

import com.unl.cajero.dto.SaldoResponse;
import com.unl.cajero.model.Operacion;
import com.unl.cajero.service.CajeroService;
import com.unl.cajero.service.CajeroSinMutexService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cajero")
@CrossOrigin(origins = {
    "http://localhost:5500",
    "http://127.0.0.1:5500"
})
public class CajeroController {

    // Servicios del cajero
    private final CajeroService cajeroService;
    private final CajeroSinMutexService cajeroSinMutexService;

    // Constructor
    public CajeroController(
            CajeroService cajeroService,
            CajeroSinMutexService cajeroSinMutexService
    ) {
        this.cajeroService = cajeroService;
        this.cajeroSinMutexService = cajeroSinMutexService;
    }

    // Consultar saldo
    @GetMapping("/saldo")
    public SaldoResponse obtenerSaldo() {
        return new SaldoResponse(cajeroService.obtenerSaldo());
    }

    // Consultar operaciones
    @GetMapping("/operaciones")
    public List<Operacion> obtenerOperaciones() {
        return cajeroService.obtenerOperaciones();
    }

    // Simulación con mutex
    @PostMapping("/simular")
    public SaldoResponse simular() {
        double saldoFinal = cajeroService.simular();
        return new SaldoResponse(saldoFinal);
    }

    // Simulación sin mutex
    @PostMapping("/simular-sin-mutex")
    public SaldoResponse simularSinMutex() throws InterruptedException {
        double saldoFinal = cajeroSinMutexService.simularSinMutex();
        return new SaldoResponse(saldoFinal);
    }
}
