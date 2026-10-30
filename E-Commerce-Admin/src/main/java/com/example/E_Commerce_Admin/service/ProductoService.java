package com.example.E_Commerce_Admin.service;

import com.example.E_Commerce.model.Producto;
import com.example.E_Commerce_Admin.repository.ProductoRepository;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ProductoService {
    private final ProductoRepository productos;
    private final Validator validator;

    public ProductoService(ProductoRepository productos, Validator validator) {
        this.productos = productos;
        this.validator = validator;
    }

    public List<Producto> listar(String buscar) {
        return productos.findByNombreContainingIgnoreCaseOrderByIdDesc(buscar == null ? "" : buscar.trim());
    }

    public Producto obtener(Long id) {
        return productos.findById(id).orElseThrow(RecursoNoEncontradoException::new);
    }

    public Map<String, String> validar(Producto producto) {
        Map<String, String> errores = new LinkedHashMap<>();
        validator.validate(producto).forEach(error ->
                errores.put(error.getPropertyPath().toString(), error.getMessage()));
        if (producto.getNombre() != null && producto.getNombre().length() > 150)
            errores.put("nombre", "El nombre admite hasta 150 caracteres.");
        if (producto.getDescripcion() != null && producto.getDescripcion().length() > 2000)
            errores.put("descripcion", "La descripción admite hasta 2000 caracteres.");
        String imagen = producto.getImagenUrl();
        if (imagen != null && (imagen.length() > 500 || !imagen.matches("^(https?://[^\\s]+|/imgs/[^\\s]+)?$")))
            errores.put("imagenUrl", "Usa una URL http(s) o una ruta /imgs/ de hasta 500 caracteres.");
        return errores;
    }

    @Transactional
    public void guardar(Long id, Producto datos) {
        if (!validar(datos).isEmpty()) throw new IllegalArgumentException("Revisa los datos del producto.");
        var producto = id == null ? new Producto() : obtener(id);
        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setImagenUrl(datos.getImagenUrl());
        producto.setActivo(datos.isActivo());
        productos.save(producto);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        var producto = obtener(id);
        producto.setActivo(!producto.isActivo());
        productos.save(producto);
    }
}
