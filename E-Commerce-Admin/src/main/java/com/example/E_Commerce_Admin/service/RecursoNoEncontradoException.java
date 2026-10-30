package com.example.E_Commerce_Admin.service;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException() { super("El recurso solicitado no existe."); }
}
