package com.uam.prestamos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los JWT (HS256).
 * subject = username, claim "rol" = ADMIN | USER, iat y exp.
 * El secreto sale de JWT_SECRET (backend/.env), nunca del código.
 */
@Service
public class JwtService {

    private static final int LONGITUD_MINIMA_SECRETO = 32;

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret:}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        if (secreto == null || secreto.isBlank() || secreto.length() < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres; configúrelo en backend/.env");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /** Lanza JwtException si la firma es inválida, el token está mal formado o expiró. */
    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, UserDetails user) {
        Claims claims = extraerClaims(token);
        return claims.getSubject().equals(user.getUsername())
                && claims.getExpiration().after(new Date());
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }

    private Claims extraerClaims(String token) {
        // verifyWith comprueba la firma; parseSignedClaims rechaza tokens expirados (ExpiredJwtException)
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
