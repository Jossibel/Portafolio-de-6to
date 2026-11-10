package com.unl.fabrica_service.model;

// lo que hace cada hilo, se manda al frontend
public record EventoSimulacion(long tMs, TipoEvento tipo, String actor, Integer piezaId,
                               int ocupacion, String hilo, String mensaje) {}
