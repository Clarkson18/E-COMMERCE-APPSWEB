package com.example.E_Commerce.dto;

import com.example.E_Commerce.model.Resenia;

import java.time.LocalDateTime;

/**
 * DTO de salida para las reseñas públicas de un producto (sin exponer datos sensibles del autor).
 */
public record ReseniaResponse(
        Long id,
        Long productoId,
        String autorNombre,
        Integer calificacion,
        String comentario,
        LocalDateTime fecha) {

    public static ReseniaResponse desdeEntidad(Resenia resenia) {
        return new ReseniaResponse(
                resenia.getId(),
                resenia.getProducto().getId(),
                resenia.getUsuario().getNombre(),
                resenia.getCalificacion(),
                resenia.getComentario(),
                resenia.getFecha());
    }
}
