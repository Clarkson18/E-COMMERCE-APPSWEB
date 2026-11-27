package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.CrearReseniaRequest;
import com.example.E_Commerce.dto.ReseniaResponse;
import com.example.E_Commerce.security.UsuarioAutenticado;
import com.example.E_Commerce.service.ReseniaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar y publicar reseñas asociadas a un producto.
 */
@RestController
@RequestMapping("/api/productos/{productoId}/resenias")
public class ReseniaRestController {

    private final ReseniaService reseniaService;

    public ReseniaRestController(ReseniaService reseniaService) {
        this.reseniaService = reseniaService;
    }

    @GetMapping
    public ResponseEntity<List<ReseniaResponse>> listar(@PathVariable Long productoId) {
        return ResponseEntity.ok(reseniaService.listarPorProducto(productoId));
    }

    @PostMapping
    public ResponseEntity<ReseniaResponse> crear(
            @PathVariable Long productoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CrearReseniaRequest request) {
        ReseniaResponse creada = reseniaService.crear(productoId, usuario.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
