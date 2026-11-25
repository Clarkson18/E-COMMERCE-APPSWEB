package com.example.E_Commerce.security;

import com.example.E_Commerce.model.RolUsuario;
import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Principal autenticado extraído de un token JWT válido.
 */
public record UsuarioAutenticado(Long id, String email, RolUsuario rol) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return email;
    }
}
