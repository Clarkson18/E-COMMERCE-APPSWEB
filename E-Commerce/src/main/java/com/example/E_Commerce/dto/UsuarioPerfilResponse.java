package com.example.E_Commerce.dto;

import com.example.E_Commerce.model.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO de salida con la información pública del perfil de usuario (sin contraseña).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioPerfilResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String direccion,
        String rol,
        LocalDateTime fechaRegistro,
        String tokenRenovado) {

    public static UsuarioPerfilResponse desdeEntidad(Usuario usuario) {
        return desdeEntidadConToken(usuario, null);
    }

    public static UsuarioPerfilResponse desdeEntidadConToken(Usuario usuario, String tokenRenovado) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getRol().name(),
                usuario.getFechaRegistro(),
                tokenRenovado);
    }
}
