package com.example.E_Commerce_Admin.config;

import com.example.E_Commerce_Admin.service.AdministradorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AdministradorService administradores) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/admin/login", "/styles/**", "/imgs/**").permitAll()
                .requestMatchers("/admin", "/admin/**", "/").access((authentication, context) -> {
                    var usuario = authentication.get();
                    boolean autorizado = usuario.isAuthenticated()
                            && usuario.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                            && administradores.tieneAcceso(usuario.getName());
                    return new AuthorizationDecision(autorizado);
                })
                .anyRequest().denyAll())
            .formLogin(login -> login.loginPage("/admin/login")
                .loginProcessingUrl("/admin/login").usernameParameter("email")
                .defaultSuccessUrl("/admin", true).failureUrl("/admin/login?error").permitAll())
            .logout(logout -> logout.logoutUrl("/admin/logout")
                .invalidateHttpSession(true).clearAuthentication(true)
                .deleteCookies("ADMINSESSION").logoutSuccessUrl("/admin/login?logout"));
        // CSRF queda habilitado. Thymeleaf incluye el token en formularios POST.
        return http.build();
    }
}
