package com.unl.puente_service.policy;

import com.unl.puente_service.model.Direccion;
import com.unl.puente_service.model.Puente;

// la logica de la guia: entra si el puente esta libre o va en su misma direccion.
// Problema: si siguen llegando autos de un lado, el otro lado espera mucho (inanicion)
public class PoliticaBase implements PoliticaAcceso {

    @Override
    public String nombre() {
        return "base";
    }

    @Override
    public boolean puedeEntrar(Direccion d, Puente p) {
        return p.getDireccion() == Direccion.NINGUNA || p.getDireccion() == d;
    }
}
