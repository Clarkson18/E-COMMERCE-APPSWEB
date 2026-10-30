package com.example.E_Commerce_Admin.controllers;

import com.example.E_Commerce_Admin.service.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ErroresControllerAdvice {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado() { return "error/404"; }
}
