package com.example.E_Commerce.exception;

/**
 * Excepción lanzada cuando una petición incumple una regla de negocio (HTTP 400).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
