package com.example.E_Commerce_Admin.service;

import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce_Admin.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdministradorService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    public AdministradorService(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .filter(u -> u.isActivo() && u.getRol() == RolUsuario.ADMIN)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
        return User.withUsername(usuario.getEmail()).password(usuario.getContrasenia())
                .roles("ADMIN").build();
    }

    // Revalidar el estado de la cuenta también durante una sesión existente.
    public boolean tieneAcceso(String email) {
        return usuarios.findByEmailIgnoreCase(email)
                .map(u -> u.isActivo() && u.getRol() == RolUsuario.ADMIN).orElse(false);
    }
}
