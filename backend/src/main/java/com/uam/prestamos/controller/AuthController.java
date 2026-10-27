package com.uam.prestamos.controller;

import com.uam.prestamos.dto.auth.LoginRequest;
import com.uam.prestamos.dto.auth.LoginResponse;
import com.uam.prestamos.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /api/auth/login (público): verifica usuario y contraseña con BCrypt y devuelve un JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String PREFIJO_ROL = "ROLE_";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            // No se indica si falló el usuario o la contraseña
            return credencialesInvalidas(httpRequest);
        }

        String rol = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(PREFIJO_ROL))
                .map(a -> a.substring(PREFIJO_ROL.length()))
                .findFirst()
                .orElseThrow();

        String username = autenticacion.getName();
        String token = jwtService.generarToken(username, rol);
        return ResponseEntity.ok(new LoginResponse(token, username, rol, jwtService.getExpiracionMs()));
    }

    private ResponseEntity<Map<String, Object>> credencialesInvalidas(HttpServletRequest httpRequest) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().withNano(0));
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("message", "Credenciales inválidas");
        cuerpo.put("path", httpRequest.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
