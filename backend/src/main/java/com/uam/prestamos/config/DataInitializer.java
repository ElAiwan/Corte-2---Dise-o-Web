package com.uam.prestamos.config;

import com.uam.prestamos.model.Rol;
import com.uam.prestamos.model.Usuario;
import com.uam.prestamos.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea las cuentas de prueba al arrancar (si no existen), con la contraseña cifrada en BCrypt.
 * Las contraseñas vienen de ADMIN_PASSWORD / USER_PASSWORD y nunca se imprimen en el log.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String userPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seed.admin-password}") String adminPassword,
                           @Value("${app.seed.user-password}") String userPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.userPassword = userPassword;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", adminPassword, Rol.ADMIN);
        crearSiNoExiste("usuario", userPassword, Rol.USER);
    }

    private void crearSiNoExiste(String username, String password, Rol rol) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        usuarioRepository.save(new Usuario(username, passwordEncoder.encode(password), rol));
        log.info("Usuario de prueba creado: {} ({})", username, rol);
    }
}
