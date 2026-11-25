package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.ActualizarPerfilRequest;
import com.example.E_Commerce.dto.UsuarioPerfilResponse;
import com.example.E_Commerce.security.UsuarioAutenticado;
import com.example.E_Commerce.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar y actualizar el perfil del cliente autenticado.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilRestController {

    public static final String HEADER_NUEVO_TOKEN = "X-New-Token";

    private final PerfilService perfilService;

    public PerfilRestController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(perfilService.obtenerPerfil(usuario.id()));
    }

    @PutMapping
    public ResponseEntity<UsuarioPerfilResponse> actualizarPerfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        UsuarioPerfilResponse respuesta = perfilService.actualizarPerfil(usuario.id(), request);
        if (respuesta.tokenRenovado() != null) {
            return ResponseEntity.ok()
                    .header(HEADER_NUEVO_TOKEN, respuesta.tokenRenovado())
                    .body(respuesta);
        }
        return ResponseEntity.ok(respuesta);
    }
}
