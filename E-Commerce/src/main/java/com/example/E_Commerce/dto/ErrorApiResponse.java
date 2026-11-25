package com.example.E_Commerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representación JSON uniforme para respuestas de error de la API REST.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorApiResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> erroresValidacion) {

    public static ErrorApiResponse of(int status, String error, String mensaje, String ruta) {
        return new ErrorApiResponse(LocalDateTime.now(), status, error, mensaje, ruta, null);
    }

    public static ErrorApiResponse conValidaciones(
            int status,
            String error,
            String mensaje,
            String ruta,
            Map<String, String> erroresValidacion) {
        return new ErrorApiResponse(LocalDateTime.now(), status, error, mensaje, ruta, erroresValidacion);
    }
}
