package com.example.E_Commerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar una reseña de producto.
 */
public record CrearReseniaRequest(
        @NotNull(message = "La calificación es obligatoria.")
        @Min(value = 1, message = "La calificación mínima es 1.")
        @Max(value = 5, message = "La calificación máxima es 5.")
        Integer calificacion,

        @NotBlank(message = "El comentario es obligatorio.")
        @Size(max = 2000, message = "El comentario no puede exceder 2000 caracteres.")
        String comentario) {
}
