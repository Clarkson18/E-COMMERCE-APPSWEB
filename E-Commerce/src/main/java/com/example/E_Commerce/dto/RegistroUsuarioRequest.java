package com.example.E_Commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el registro de nuevos clientes.
 */
public record RegistroUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.")
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres.")
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "El teléfono tiene un formato inválido.")
        String telefono,

        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El correo electrónico debe tener un formato válido.")
        @Size(max = 254, message = "El correo electrónico no puede exceder 254 caracteres.")
        String email,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 500, message = "La dirección no puede exceder 500 caracteres.")
        String direccion,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String contrasenia,

        @NotBlank(message = "La confirmación de contraseña es obligatoria.")
        String confirmacionContrasenia) {
}
