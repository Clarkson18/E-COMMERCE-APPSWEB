package com.example.E_Commerce.exception;

/**
 * Excepción lanzada ante conflictos de estado o unicidad (HTTP 409).
 */
public class ConflictoNegocioException extends RuntimeException {

    public ConflictoNegocioException(String mensaje) {
        super(mensaje);
    }
}
