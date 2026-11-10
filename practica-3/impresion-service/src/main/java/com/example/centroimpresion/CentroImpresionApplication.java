package com.example.centroimpresion;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.centroimpresion.command.DisenoEtapa;
import com.example.centroimpresion.command.ImpresionEtapa;
import com.example.centroimpresion.command.RevisionEtapa;
import com.example.centroimpresion.service.SincronizadorService;

@SpringBootApplication
public class CentroImpresionApplication implements CommandLineRunner {

    private final SincronizadorService sincronizadorService;

    public CentroImpresionApplication(SincronizadorService sincronizadorService) {
        this.sincronizadorService = sincronizadorService;
    }

    public static void main(String[] genealogy) {
        SpringApplication.run(CentroImpresionApplication.class, genealogy);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("INICIANDO PRÁCTICA DE CONCURRENCIA - EJERCICIO 5\n");

        // Instanciar los comandos pasando el servicio inyectado
        Thread hiloDiseno = new Thread(new DisenoEtapa(sincronizadorService, 0));
        Thread hiloRevision = new Thread(new RevisionEtapa(sincronizadorService, 0));
        Thread hiloImpresion = new Thread(new ImpresionEtapa(sincronizadorService, 0));

        // Lanzar los procesos en paralelo
        hiloDiseno.start();
        hiloRevision.start();
        hiloImpresion.start();

        // Esperar a que terminen todas las iteraciones
        hiloDiseno.join();
        hiloRevision.join();
        hiloImpresion.join();

        System.out.println("Simulación terminada con éxito. Todos los documentos procesados.");
    }
}
