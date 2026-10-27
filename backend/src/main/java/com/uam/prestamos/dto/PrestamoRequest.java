package com.uam.prestamos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PrestamoRequest(
        @NotNull(message = "El equipo es obligatorio")
        @Positive(message = "El id del equipo debe ser positivo")
        Long equipoId,
        @NotBlank(message = "El nombre del solicitante es obligatorio")
        @Size(max = 100, message = "El nombre del solicitante admite máximo 100 caracteres")
        String nombreSolicitante,
        @NotNull(message = "La fecha del préstamo es obligatoria")
        LocalDate fechaPrestamo,
        @NotNull(message = "La fecha prevista de devolución es obligatoria")
        LocalDate fechaDevolucionPrevista) {
}
