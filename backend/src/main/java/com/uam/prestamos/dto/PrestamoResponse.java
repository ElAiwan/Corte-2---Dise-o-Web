package com.uam.prestamos.dto;

import com.uam.prestamos.model.Equipo;
import com.uam.prestamos.model.EstadoPrestamo;
import com.uam.prestamos.model.Prestamo;

import java.time.LocalDate;

public record PrestamoResponse(Long id, Long equipoId, String equipoCodigo, String equipoNombre,
                               String nombreSolicitante, LocalDate fechaPrestamo,
                               LocalDate fechaDevolucionPrevista, LocalDate fechaDevolucionReal,
                               EstadoPrestamo estado) {

    // Se ejecuta dentro de la transacción del servicio para acceder a la relación LAZY.
    public static PrestamoResponse from(Prestamo prestamo) {
        Equipo equipo = prestamo.getEquipo();
        return new PrestamoResponse(prestamo.getId(), equipo.getId(),
                equipo.getCodigoPatrimonial(), equipo.getNombre(), prestamo.getNombreSolicitante(),
                prestamo.getFechaPrestamo(), prestamo.getFechaDevolucionPrevista(),
                prestamo.getFechaDevolucionReal(), prestamo.getEstado());
    }
}
