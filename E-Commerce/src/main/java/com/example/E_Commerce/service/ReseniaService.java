package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.CrearReseniaRequest;
import com.example.E_Commerce.dto.ReseniaResponse;
import com.example.E_Commerce.exception.AccesoDenegadoNegocioException;
import com.example.E_Commerce.model.Producto;
import com.example.E_Commerce.model.Resenia;
import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.ReseniaRepository;
import com.example.E_Commerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para consultar y publicar reseñas de productos activos.
 */
@Service
@Transactional(readOnly = true)
public class ReseniaService {

    private final ReseniaRepository resenias;
    private final ProductoService productoService;
    private final UsuarioRepository usuarios;

    public ReseniaService(
            ReseniaRepository resenias,
            ProductoService productoService,
            UsuarioRepository usuarios) {
        this.resenias = resenias;
        this.productoService = productoService;
        this.usuarios = usuarios;
    }

    public List<ReseniaResponse> listarPorProducto(Long productoId) {
        productoService.obtenerEntidadActiva(productoId);
        return resenias.findByProductoIdOrderByFechaDescIdDesc(productoId).stream()
                .map(ReseniaResponse::desdeEntidad)
                .toList();
    }

    @Transactional
    public ReseniaResponse crear(Long productoId, Long usuarioId, CrearReseniaRequest request) {
        Producto producto = productoService.obtenerEntidadActiva(productoId);
        Usuario usuario = usuarios.findById(usuarioId)
                .filter(u -> u.isActivo() && u.getRol() == RolUsuario.CLIENTE)
                .orElseThrow(() -> new AccesoDenegadoNegocioException("La cuenta de cliente no se encuentra activa."));

        Resenia resenia = new Resenia();
        resenia.setUsuario(usuario);
        resenia.setProducto(producto);
        resenia.setCalificacion(request.calificacion());
        resenia.setComentario(request.comentario().trim());
        resenia.setFecha(LocalDateTime.now());

        Resenia guardada = resenias.save(resenia);
        return ReseniaResponse.desdeEntidad(guardada);
    }
}
