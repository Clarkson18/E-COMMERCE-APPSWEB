package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.CrearSesionRequest;
import com.example.E_Commerce.dto.RegistroUsuarioRequest;
import com.example.E_Commerce.dto.SesionTokenResponse;
import com.example.E_Commerce.dto.UsuarioPerfilResponse;
import com.example.E_Commerce.exception.AccesoDenegadoNegocioException;
import com.example.E_Commerce.exception.ConflictoNegocioException;
import com.example.E_Commerce.exception.CredencialesInvalidasException;
import com.example.E_Commerce.exception.ReglaNegocioException;
import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.UsuarioRepository;
import com.example.E_Commerce.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Servicio de registro e inicio de sesión para clientes del storefront.
 */
@Service
@Transactional(readOnly = true)
public class ClienteAuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ClienteAuthService(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioPerfilResponse registrar(RegistroUsuarioRequest request) {
        if (request.contrasenia().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException("La contraseña no puede exceder 72 bytes UTF-8.");
        }
        if (!request.contrasenia().equals(request.confirmacionContrasenia())) {
            throw new ReglaNegocioException("La contraseña y su confirmación no coinciden.");
        }

        String emailNormalizado = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ConflictoNegocioException("Ya existe una cuenta registrada con ese correo electrónico.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setTelefono(request.telefono().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setDireccion(request.direccion().trim());
        usuario.setContrasenia(passwordEncoder.encode(request.contrasenia()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario guardado = usuarios.save(usuario);
        return UsuarioPerfilResponse.desdeEntidad(guardado);
    }

    public SesionTokenResponse iniciarSesion(CrearSesionRequest request) {
        if (request.contrasenia().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new CredencialesInvalidasException("Credenciales inválidas.");
        }

        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas."));

        if (!passwordEncoder.matches(request.contrasenia(), usuario.getContrasenia())) {
            throw new CredencialesInvalidasException("Credenciales inválidas.");
        }

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new CredencialesInvalidasException("Credenciales inválidas.");
        }

        if (!usuario.isActivo()) {
            throw new AccesoDenegadoNegocioException("La cuenta de cliente se encuentra desactivada.");
        }

        String token = jwtService.generarToken(usuario);
        return new SesionTokenResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                UsuarioPerfilResponse.desdeEntidad(usuario));
    }
}
