package com.unl.puente_service.policy;

import com.unl.puente_service.model.Direccion;
import com.unl.puente_service.model.Puente;

// patron Strategy: la regla para decidir si un vehiculo puede entrar
public interface PoliticaAcceso {
    String nombre();

    boolean puedeEntrar(Direccion d, Puente puente);

    // cuando sale el ultimo vehiculo
    default void alQuedarLibre(Direccion salio, Puente puente) {}
}
