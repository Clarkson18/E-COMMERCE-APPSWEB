package com.example.E_Commerce.config;

import com.example.E_Commerce.dto.ErrorApiResponse;
import com.example.E_Commerce.security.JwtAuthenticationFilter;
import com.example.E_Commerce.security.UsuarioSeguridadService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            UsuarioSeguridadService usuariosSeguridad,
            JsonMapper jsonMapper) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/sesiones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/*", "/api/productos/*/resenias")
                        .permitAll()
                        .anyRequest().access((authentication, context) -> {
                            var actual = authentication.get();
                            if (actual == null || actual instanceof AnonymousAuthenticationToken || !actual.isAuthenticated()) {
                                return new AuthorizationDecision(false);
                            }
                            boolean autorizado = actual.getAuthorities().stream()
                                    .anyMatch(ga -> ga.getAuthority().equals("ROLE_CLIENTE"))
                                    && usuariosSeguridad.tieneAccesoCliente(actual.getName());
                            return new AuthorizationDecision(autorizado);
                        }))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String mensajeJwt = (String) request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERROR_JWT);
                            String mensaje = mensajeJwt != null
                                    ? mensajeJwt
                                    : "Se requiere autenticación mediante un token Bearer válido.";
                            escribirErrorJson(
                                    response,
                                    jsonMapper,
                                    HttpStatus.UNAUTHORIZED,
                                    mensaje,
                                    request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> escribirErrorJson(
                                response,
                                jsonMapper,
                                HttpStatus.FORBIDDEN,
                                "No tienes permisos para realizar esta acción o tu cuenta se encuentra inactiva.",
                                request.getRequestURI())))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/styles/**", "/imgs/**", "/js/**", "/**").permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    private void escribirErrorJson(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            HttpStatus status,
            String mensaje,
            String ruta) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorApiResponse body = ErrorApiResponse.of(
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                ruta);
        jsonMapper.writeValue(response.getWriter(), body);
    }
}
