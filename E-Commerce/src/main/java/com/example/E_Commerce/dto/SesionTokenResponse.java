package com.example.E_Commerce.dto;

/**
 * DTO de salida para el inicio de sesión exitoso con token JWT.
 */
public record SesionTokenResponse(
        String token,
        String tipo,
        long expiraEnMs,
        UsuarioPerfilResponse usuario) {
}
