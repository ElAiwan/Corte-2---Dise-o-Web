package com.uam.prestamos.dto.auth;

/**
 * Respuesta 200 de POST /api/auth/login.
 * El cliente envía "token" en el header: Authorization: Bearer &lt;token&gt;.
 */
public record LoginResponse(String token, String tipo, String username, String rol, long expiraEnMs) {

    public LoginResponse(String token, String username, String rol, long expiraEnMs) {
        this(token, "Bearer", username, rol, expiraEnMs);
    }
}
