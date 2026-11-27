package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.ProductoDetalleResponse;
import com.example.E_Commerce.dto.ProductoResumenResponse;
import com.example.E_Commerce.service.ProductoService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST público para consulta del catálogo y detalle de productos.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoService productoService;

    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResumenResponse>> listar(
            @RequestParam(required = false)
            @Size(max = 150, message = "El filtro de nombre no puede exceder 150 caracteres.")
            String nombre,

            @RequestParam(required = false)
            @DecimalMin(value = "0.00", message = "El precio máximo no puede ser negativo.")
            BigDecimal precioMaximo,

            @RequestParam(required = false)
            String orden) {
        return ResponseEntity.ok(productoService.listarActivos(nombre, precioMaximo, orden));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDetalleResponse> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerDetalleActivo(id));
    }
}
