package com.unl.puente_service.model;

// lo que hace cada hilo vehiculo, se manda al frontend
public record EventoPuente(long tMs, TipoEvento tipo, String vehiculo, Direccion direccion,
                           int enNorte, int enSur, String hilo, String mensaje) {}
