package com.example.E_Commerce.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe o no está disponible.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException() {
        super("El recurso solicitado no existe.");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
