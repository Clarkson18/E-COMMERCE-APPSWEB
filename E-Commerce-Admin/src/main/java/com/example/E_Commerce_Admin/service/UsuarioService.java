package com.example.E_Commerce_Admin.service;

import com.example.E_Commerce.model.*;
import com.example.E_Commerce_Admin.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarios;
    public UsuarioService(UsuarioRepository usuarios) { this.usuarios = usuarios; }
    public List<Usuario> listar() { return usuarios.findAllByOrderByIdDesc(); }
    @Transactional
    public void cambiarEstado(Long id) {
        var usuario = usuarios.findById(id).orElseThrow(RecursoNoEncontradoException::new);
        if (usuario.getRol() == RolUsuario.ADMIN) throw new IllegalArgumentException("Las cuentas administradoras no se desactivan desde este panel.");
        usuario.setActivo(!usuario.isActivo()); usuarios.save(usuario);
    }
}
