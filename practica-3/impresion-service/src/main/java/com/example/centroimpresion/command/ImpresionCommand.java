package com.example.centroimpresion.command;

// Definición del patrón Command para desacoplar la ejecución
public interface ImpresionCommand extends Runnable {
    void ejecutarEtapa() throws InterruptedException;
}
