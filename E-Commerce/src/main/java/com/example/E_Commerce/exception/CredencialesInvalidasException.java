package com.example.E_Commerce.exception;

/**
 * Excepción lanzada cuando las credenciales de autenticación son inválidas (HTTP 401).
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
