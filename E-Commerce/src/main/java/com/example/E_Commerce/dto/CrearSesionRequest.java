package com.example.E_Commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la creación de sesión (autenticación JWT).
 */
public record CrearSesionRequest(
        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El correo electrónico debe tener un formato válido.")
        @Size(max = 254, message = "El correo electrónico no puede exceder 254 caracteres.")
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(max = 72, message = "La contraseña no puede exceder 72 caracteres.")
        String contrasenia) {
}
