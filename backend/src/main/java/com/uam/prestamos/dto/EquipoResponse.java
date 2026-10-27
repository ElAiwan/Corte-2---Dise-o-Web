package com.uam.prestamos.dto;

import com.uam.prestamos.model.Equipo;
import com.uam.prestamos.model.EstadoEquipo;
import com.uam.prestamos.model.TipoEquipo;

public record EquipoResponse(Long id, String codigoPatrimonial, String nombre,
                             TipoEquipo tipo, EstadoEquipo estado) {

    public static EquipoResponse from(Equipo equipo) {
        return new EquipoResponse(equipo.getId(), equipo.getCodigoPatrimonial(),
                equipo.getNombre(), equipo.getTipo(), equipo.getEstado());
    }
}
