package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.RegistroUsuarioRequest;
import com.example.E_Commerce.dto.UsuarioPerfilResponse;
import com.example.E_Commerce.service.ClienteAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el registro de cuentas de cliente.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final ClienteAuthService clienteAuthService;

    public UsuarioRestController(ClienteAuthService clienteAuthService) {
        this.clienteAuthService = clienteAuthService;
    }

    @PostMapping
    public ResponseEntity<UsuarioPerfilResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        UsuarioPerfilResponse creado = clienteAuthService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
