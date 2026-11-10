package com.unl.puente_service.policy;

import com.unl.puente_service.model.Direccion;
import com.unl.puente_service.model.Puente;

// RETO: maximo "limite" autos seguidos por lado SI hay alguien esperando del otro lado.
// Cuando el puente se vacia le toca al otro lado. Si nadie espera enfrente se deja pasar normal.
public class PoliticaJusta implements PoliticaAcceso {

    private final int limite;

    public PoliticaJusta(int limite) {
        this.limite = Math.max(1, limite);
    }

    @Override
    public String nombre() {
        return "justa";
    }

    @Override
    public boolean puedeEntrar(Direccion d, Puente p) {
        boolean alguienEnfrente = p.esperando(d.contraria()) > 0;

        if (p.getDireccion() == Direccion.NINGUNA) {
            // puente libre: entra si es su turno, si no hay turno o si nadie espera enfrente
            return p.getTurno() == Direccion.NINGUNA || p.getTurno() == d || !alguienEnfrente;
        }
        if (p.getDireccion() == d) {
            // misma direccion: puede seguir hasta llegar al limite
            return !alguienEnfrente || p.getConsecutivos() < limite;
        }
        return false; // va en sentido contrario
    }

    @Override
    public void alQuedarLibre(Direccion salio, Puente p) {
        Direccion otra = salio.contraria();
        p.setTurno(p.esperando(otra) > 0 ? otra : Direccion.NINGUNA);
    }
}
