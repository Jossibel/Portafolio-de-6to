package com.unl.puente_service.model;

// llegadaMs = en que momento llega al puente
public record Vehiculo(String id, Direccion direccion, long llegadaMs) {}
