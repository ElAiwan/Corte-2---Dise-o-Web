package com.uam.prestamos.security;

import com.uam.prestamos.model.Usuario;
import com.uam.prestamos.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga al usuario desde la tabla "usuarios" para Spring Security.
 * La authority es "ROLE_" + rol (ROLE_ADMIN / ROLE_USER), por eso en SecurityConfig se usa hasRole("ADMIN").
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword()) // hash BCrypt
                .authorities("ROLE_" + usuario.getRol().name())
                .build();
    }
}
