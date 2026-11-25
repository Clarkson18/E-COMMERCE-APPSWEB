package com.example.E_Commerce.security;

import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio encargado de generar y validar tokens JWT firmados con HMAC-SHA256.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("La propiedad app.jwt.secret debe tener al menos 32 bytes UTF-8.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UsuarioAutenticado extraerUsuario(String token) {
        Claims claims = extraerClaims(token);
        String email = claims.getSubject();
        Number uid = claims.get("uid", Number.class);
        String rol = claims.get("rol", String.class);

        if (email == null || email.isBlank() || uid == null || rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("El token JWT no contiene los claims requeridos.");
        }

        return new UsuarioAutenticado(uid.longValue(), email, RolUsuario.valueOf(rol));
    }

    public boolean esTokenValido(String token) {
        try {
            extraerUsuario(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
