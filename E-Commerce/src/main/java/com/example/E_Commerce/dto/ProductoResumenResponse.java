package com.example.E_Commerce.dto;

import com.example.E_Commerce.model.Producto;

import java.math.BigDecimal;

/**
 * DTO de salida resumido para los productos mostrados en el catálogo.
 */
public record ProductoResumenResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        boolean disponible,
        String imagenUrl) {

    public static ProductoResumenResponse desdeEntidad(Producto producto) {
        boolean disponible = producto.isActivo()
                && producto.getStock() != null
                && producto.getStock() > 0;
        return new ProductoResumenResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                disponible,
                producto.getImagenUrl());
    }
}
