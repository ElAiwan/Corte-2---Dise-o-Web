package com.uam.prestamos.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @ParameterizedTest
    @ValueSource(ints = {32, 48, 64, 96})
    void respetaHs256ConDistintasLongitudesDeSecreto(int longitud) {
        String secreto = "s".repeat(longitud);
        JwtService servicio = new JwtService(secreto, 3_600_000);

        var jwt = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(servicio.generarToken("admin", "ADMIN"));

        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(jwt.getPayload().getSubject()).isEqualTo("admin");
        assertThat(jwt.getPayload().get("rol", String.class)).isEqualTo("ADMIN");
    }
}
