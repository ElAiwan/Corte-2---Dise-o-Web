package com.uam.prestamos.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de POST /api/auth/login.
 * Si algún campo llega vacío, @Valid lanza MethodArgumentNotValidException
 * y el GlobalExceptionHandler responde 400.
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
