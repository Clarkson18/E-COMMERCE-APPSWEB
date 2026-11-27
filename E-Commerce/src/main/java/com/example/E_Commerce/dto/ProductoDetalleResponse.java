package com.example.E_Commerce.dto;

import com.example.E_Commerce.model.Producto;

import java.math.BigDecimal;

/**
 * DTO de salida con el detalle completo y calificación agregada de un producto activo.
 */
public record ProductoDetalleResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        boolean disponible,
        String imagenUrl,
        double calificacionPromedio,
        long totalResenias) {

    public static ProductoDetalleResponse desdeEntidad(
            Producto producto,
            double calificacionPromedio,
            long totalResenias) {
        boolean disponible = producto.isActivo()
                && producto.getStock() != null
                && producto.getStock() > 0;
        return new ProductoDetalleResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                disponible,
                producto.getImagenUrl(),
                calificacionPromedio,
                totalResenias);
    }
}
