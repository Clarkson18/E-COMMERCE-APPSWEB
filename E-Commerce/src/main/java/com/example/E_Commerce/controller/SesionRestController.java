package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.CrearSesionRequest;
import com.example.E_Commerce.dto.SesionTokenResponse;
import com.example.E_Commerce.service.ClienteAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la creación de sesiones mediante autenticación JWT.
 */
@RestController
@RequestMapping("/api/sesiones")
public class SesionRestController {

    private final ClienteAuthService clienteAuthService;

    public SesionRestController(ClienteAuthService clienteAuthService) {
        this.clienteAuthService = clienteAuthService;
    }

    @PostMapping
    public ResponseEntity<SesionTokenResponse> crearSesion(@Valid @RequestBody CrearSesionRequest request) {
        return ResponseEntity.ok(clienteAuthService.iniciarSesion(request));
    }
}
