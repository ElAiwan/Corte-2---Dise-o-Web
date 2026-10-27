package com.uam.prestamos.repository;

import com.uam.prestamos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Lo usa la seguridad para cargar al usuario en el login. */
    Optional<Usuario> findByUsername(String username);

    /** Lo usa DataInitializer para no duplicar las cuentas de prueba. */
    boolean existsByUsername(String username);
}
