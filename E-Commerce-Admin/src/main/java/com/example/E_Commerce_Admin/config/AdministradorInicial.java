package com.example.E_Commerce_Admin.config;

import com.example.E_Commerce.model.*;
import com.example.E_Commerce_Admin.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(name = "app.admin-inicial.habilitado", havingValue = "true")
public class AdministradorInicial {
    @Bean
    CommandLineRunner crearAdministrador(UsuarioRepository usuarios, PasswordEncoder encoder, Environment env) {
        return args -> {
            String email = env.getRequiredProperty("app.admin-inicial.email").trim();
            String clave = env.getRequiredProperty("app.admin-inicial.password");
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 254)
                throw new IllegalArgumentException("Configura un email válido para el administrador inicial.");
            if (clave.length() < 12 || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
                throw new IllegalArgumentException("La clave inicial debe tener al menos 12 caracteres y como máximo 72 bytes.");
            if (usuarios.findByEmailIgnoreCase(email).isPresent())
                throw new IllegalStateException("El email inicial ya existe. Desactiva app.admin-inicial.habilitado; no se modificó la cuenta.");
            var u = new Usuario(); u.setNombre("Administrador"); u.setEmail(email);
            u.setContrasenia(encoder.encode(clave)); u.setRol(RolUsuario.ADMIN);
            usuarios.save(u);
        };
    }
}
