package com.unl.puente_service.dto;

// politica base vs politica justa con el mismo escenario
public record AnalisisInanicion(ResultadoPuente base, ResultadoPuente justa, double reduccionEsperaSurPorcentaje) {}
