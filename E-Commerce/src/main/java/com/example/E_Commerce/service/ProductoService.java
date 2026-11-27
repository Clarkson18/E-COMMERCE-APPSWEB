package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.ProductoDetalleResponse;
import com.example.E_Commerce.dto.ProductoResumenResponse;
import com.example.E_Commerce.exception.RecursoNoEncontradoException;
import com.example.E_Commerce.exception.ReglaNegocioException;
import com.example.E_Commerce.model.Producto;
import com.example.E_Commerce.repository.ProductoRepository;
import com.example.E_Commerce.repository.ReseniaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de consulta del catálogo público de productos activos.
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productos;
    private final ReseniaRepository resenias;

    public ProductoService(ProductoRepository productos, ReseniaRepository resenias) {
        this.productos = productos;
        this.resenias = resenias;
    }

    public List<ProductoResumenResponse> listarActivos(
            String nombre,
            BigDecimal precioMaximo,
            String orden) {
        Sort sort = resolverOrden(orden);

        Specification<Producto> spec = (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isTrue(root.get("activo")));
            if (nombre != null && !nombre.isBlank()) {
                String patronEscapado = escaparLike(nombre.trim().toLowerCase(Locale.ROOT));
                predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + patronEscapado + "%", '\\'));
            }
            if (precioMaximo != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("precio"), precioMaximo));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };

        return productos.findAll(spec, sort).stream()
                .map(ProductoResumenResponse::desdeEntidad)
                .toList();
    }

    public ProductoDetalleResponse obtenerDetalleActivo(Long id) {
        Producto producto = obtenerEntidadActiva(id);
        long totalResenias = resenias.countByProductoId(id);
        double promedio = 0.0;
        if (totalResenias > 0) {
            Double promedioBruto = resenias.obtenerPromedioPorProductoId(id);
            if (promedioBruto != null) {
                promedio = BigDecimal.valueOf(promedioBruto)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }
        }
        return ProductoDetalleResponse.desdeEntidad(producto, promedio, totalResenias);
    }

    public Producto obtenerEntidadActiva(Long id) {
        return productos.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado o no disponible."));
    }

    private Sort resolverOrden(String orden) {
        if (orden == null || orden.isBlank()) {
            return Sort.by(Sort.Order.desc("precio"), Sort.Order.desc("id"));
        }
        String valor = orden.trim().toLowerCase(Locale.ROOT);
        if ("desc".equals(valor) || "mayor".equals(valor)) {
            return Sort.by(Sort.Order.desc("precio"), Sort.Order.desc("id"));
        }
        if ("asc".equals(valor) || "menor".equals(valor)) {
            return Sort.by(Sort.Order.asc("precio"), Sort.Order.desc("id"));
        }
        throw new ReglaNegocioException("El parámetro 'orden' solo admite 'asc', 'desc', 'menor' o 'mayor'.");
    }

    private String escaparLike(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
