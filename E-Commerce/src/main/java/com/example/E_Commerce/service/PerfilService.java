package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.ActualizarPerfilRequest;
import com.example.E_Commerce.dto.UsuarioPerfilResponse;
import com.example.E_Commerce.exception.ConflictoNegocioException;
import com.example.E_Commerce.exception.RecursoNoEncontradoException;
import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.UsuarioRepository;
import com.example.E_Commerce.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Servicio para consulta y actualización del perfil del cliente autenticado.
 */
@Service
@Transactional(readOnly = true)
public class PerfilService {

    private final UsuarioRepository usuarios;
    private final JwtService jwtService;

    public PerfilService(UsuarioRepository usuarios, JwtService jwtService) {
        this.usuarios = usuarios;
        this.jwtService = jwtService;
    }

    public UsuarioPerfilResponse obtenerPerfil(Long usuarioId) {
        Usuario usuario = buscarClienteActivo(usuarioId);
        return UsuarioPerfilResponse.desdeEntidad(usuario);
    }

    @Transactional
    public UsuarioPerfilResponse actualizarPerfil(Long usuarioId, ActualizarPerfilRequest request) {
        Usuario usuario = buscarClienteActivo(usuarioId);
        String emailAnterior = usuario.getEmail();
        String nuevoEmail = request.email().trim().toLowerCase(Locale.ROOT);

        if (!emailAnterior.equalsIgnoreCase(nuevoEmail) && usuarios.existsByEmailIgnoreCase(nuevoEmail)) {
            throw new ConflictoNegocioException("Ya existe otra cuenta registrada con ese correo electrónico.");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setTelefono(request.telefono().trim());
        usuario.setEmail(nuevoEmail);
        usuario.setDireccion(request.direccion().trim());

        Usuario guardado = usuarios.save(usuario);
        if (!emailAnterior.equals(nuevoEmail)) {
            String nuevoToken = jwtService.generarToken(guardado);
            return UsuarioPerfilResponse.desdeEntidadConToken(guardado, nuevoToken);
        }
        return UsuarioPerfilResponse.desdeEntidad(guardado);
    }

    private Usuario buscarClienteActivo(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .filter(u -> u.isActivo() && u.getRol() == RolUsuario.CLIENTE)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de cliente no encontrado."));
    }
}
