package com.unl.puente_service.model;

public enum Direccion {
    NORTE, SUR, NINGUNA;

    public Direccion contraria() {
        if (this == NORTE) return SUR;
        if (this == SUR) return NORTE;
        return NINGUNA;
    }
}
