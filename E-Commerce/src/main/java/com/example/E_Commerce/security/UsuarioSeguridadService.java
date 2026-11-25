package com.example.E_Commerce.security;

import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de carga y revalidación de cuentas de cliente contra la base de datos.
 */
@Service
@Transactional(readOnly = true)
public class UsuarioSeguridadService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioSeguridadService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Credenciales inválidas.");
        }
        Usuario usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getContrasenia())
                .roles(usuario.getRol().name())
                .build();
    }

    public boolean tieneAccesoCliente(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return usuarios.findByEmailIgnoreCase(email.trim())
                .map(u -> u.isActivo() && u.getRol() == RolUsuario.CLIENTE)
                .orElse(false);
    }

    public boolean tieneAccesoClientePorId(Long id) {
        if (id == null) {
            return false;
        }
        return usuarios.findById(id)
                .map(u -> u.isActivo() && u.getRol() == RolUsuario.CLIENTE)
                .orElse(false);
    }
}
