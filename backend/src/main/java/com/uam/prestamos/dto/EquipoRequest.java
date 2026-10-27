package com.uam.prestamos.dto;

import com.uam.prestamos.model.TipoEquipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank(message = "El código patrimonial es obligatorio")
        @Size(max = 30, message = "El código patrimonial admite máximo 30 caracteres")
        String codigoPatrimonial,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombre,
        @NotNull(message = "El tipo es obligatorio (LAPTOP, PROYECTOR o TABLETA)")
        TipoEquipo tipo) {

    public EquipoRequest {
        if (codigoPatrimonial != null) {
            codigoPatrimonial = codigoPatrimonial.trim().toUpperCase(java.util.Locale.ROOT);
        }
    }
}
