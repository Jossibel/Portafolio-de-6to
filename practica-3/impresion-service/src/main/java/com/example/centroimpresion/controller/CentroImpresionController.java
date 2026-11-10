package com.example.centroimpresion.controller;

import com.example.centroimpresion.command.DisenoEtapa;
import com.example.centroimpresion.command.ImpresionEtapa;
import com.example.centroimpresion.command.RevisionEtapa;
import com.example.centroimpresion.service.SincronizadorService;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/impresion")
@CrossOrigin(origins = "*")
public class CentroImpresionController {

    private final SincronizadorService sincronizadorService;
    private static final List<String> logs = new CopyOnWriteArrayList<>();
    
    // Identificador único atómico para controlar el ciclo de vida de la ejecución activa
    public static final AtomicLong simulacionActivaId = new AtomicLong(0);

    public CentroImpresionController(SincronizadorService sincronizadorService) {
        this.sincronizadorService = sincronizadorService;
    }

    public static void agregarLog(String mensaje) {
        logs.add(mensaje);
    }

    @GetMapping("/logs")
    public List<String> obtenerLogs() {
        return new ArrayList<>(logs);
    }

    @PostMapping("/simular")
    public String iniciarSimulacion() {
        logs.clear();
        
        // Incrementar el ID: Esto invalida automáticamente todos los hilos del click anterior
        long idActual = simulacionActivaId.incrementAndGet();
        
        sincronizadorService.reiniciarSemaforos();

        // Pasamos el idActual a cada clase comando
        Thread hiloDiseno = new Thread(new DisenoEtapa(sincronizadorService, idActual));
        Thread hiloRevision = new Thread(new RevisionEtapa(sincronizadorService, idActual));
        Thread hiloImpresion = new Thread(new ImpresionEtapa(sincronizadorService, idActual));

        hiloDiseno.start();
        hiloRevision.start();
        hiloImpresion.start();

        return "Simulación " + idActual + " inicializada limpiamente";
    }
}
