package com.example.E_Commerce.exception;

/**
 * Excepción lanzada cuando el cliente autenticado no tiene permiso sobre un recurso (HTTP 403).
 */
public class AccesoDenegadoNegocioException extends RuntimeException {

    public AccesoDenegadoNegocioException(String mensaje) {
        super(mensaje);
    }
}
