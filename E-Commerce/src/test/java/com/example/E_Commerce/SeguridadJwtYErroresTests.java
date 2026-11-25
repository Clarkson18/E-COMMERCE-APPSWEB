package com.example.E_Commerce;

import com.example.E_Commerce.exception.ConflictoNegocioException;
import com.example.E_Commerce.exception.RecursoNoEncontradoException;
import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.UsuarioRepository;
import com.example.E_Commerce.security.JwtService;
import com.example.E_Commerce.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SeguridadJwtYErroresTests.ControladorPruebaFase1.class)
class SeguridadJwtYErroresTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario clienteActivo;
    private Usuario admin;

    @BeforeEach
    void prepararUsuarios() {
        usuarios.deleteAll();
        clienteActivo = crearUsuario("cliente.fase1@example.com", RolUsuario.CLIENTE, true);
        admin = crearUsuario("admin.fase1@example.com", RolUsuario.ADMIN, true);
    }

    @AfterEach
    void limpiar() {
        usuarios.deleteAll();
    }

    private Usuario crearUsuario(String email, RolUsuario rol, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Prueba");
        usuario.setEmail(email);
        usuario.setContrasenia(passwordEncoder.encode("ClavePrueba123"));
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuarios.save(usuario);
    }

    @Test
    void jwtServiceGeneraValidaYRechazaTokensInvalidosOExpirados() {
        String token = jwtService.generarToken(clienteActivo);
        assertTrue(jwtService.esTokenValido(token));

        UsuarioAutenticado autenticado = jwtService.extraerUsuario(token);
        assertEquals(clienteActivo.getId(), autenticado.id());
        assertEquals(clienteActivo.getEmail(), autenticado.email());
        assertEquals(RolUsuario.CLIENTE, autenticado.rol());

        String tokenManipulado = token + "x";
        assertFalse(jwtService.esTokenValido(tokenManipulado));

        JwtService servicioExpirado = new JwtService(
                "clave-exclusiva-pruebas-automatizadas-ecommerce-32-bytes-segura",
                -5000L);
        String tokenExpirado = servicioExpirado.generarToken(clienteActivo);
        assertFalse(jwtService.esTokenValido(tokenExpirado));
    }

    @Test
    void rechazaPeticionSinTokenOConTokenCorruptoCon401Json() throws Exception {
        mvc.perform(get("/api/prueba-fase1/privado"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.ruta").value("/api/prueba-fase1/privado"))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("at com.example"))));

        mvc.perform(get("/api/prueba-fase1/privado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-corrupto"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje", containsString("inválido")))
                .andExpect(jsonPath("$.ruta").value("/api/prueba-fase1/privado"));
    }

    @Test
    void permiteClienteActivoYRechazaInactivoOAdminCon403Json() throws Exception {
        String tokenCliente = jwtService.generarToken(clienteActivo);

        mvc.perform(get("/api/prueba-fase1/privado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteActivo.getId()))
                .andExpect(jsonPath("$.email").value(clienteActivo.getEmail()));

        clienteActivo.setActivo(false);
        usuarios.save(clienteActivo);

        mvc.perform(get("/api/prueba-fase1/privado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.ruta").value("/api/prueba-fase1/privado"));

        String tokenAdmin = jwtService.generarToken(admin);
        mvc.perform(get("/api/prueba-fase1/privado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.ruta").value("/api/prueba-fase1/privado"));
    }

    @Test
    void controllerAdviceDevuelveJsonEstructuradoSinStacktraces() throws Exception {
        String tokenCliente = jwtService.generarToken(clienteActivo);

        mvc.perform(post("/api/prueba-fase1/validacion")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erroresValidacion.nombre").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.cantidad").isNotEmpty());

        mvc.perform(post("/api/prueba-fase1/validacion")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString("inválido")));

        mvc.perform(get("/api/prueba-fase1/no-encontrado")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Producto de prueba no encontrado."));

        mvc.perform(get("/api/prueba-fase1/conflicto")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Conflicto de prueba."));

        mvc.perform(get("/api/prueba-fase1/error-interno")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error interno en el servidor."))
                .andExpect(content().string(not(containsString("Detalle sensible interno"))))
                .andExpect(content().string(not(containsString("at com.example"))));
    }

    @Test
    void conservaRutasWebExistentesDelStorefront() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/catalogo")).andExpect(status().isOk());
        mvc.perform(get("/adminPrincipal"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8081/admin"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @RestController
    @RequestMapping("/api/prueba-fase1")
    static class ControladorPruebaFase1 {

        record EntradaPrueba(@NotBlank String nombre, @Min(1) int cantidad) {
        }

        @GetMapping("/privado")
        Map<String, Object> privado(@AuthenticationPrincipal UsuarioAutenticado usuario) {
            return Map.of("id", usuario.id(), "email", usuario.email());
        }

        @PostMapping("/validacion")
        Map<String, Object> validacion(@Valid @RequestBody EntradaPrueba entrada) {
            return Map.of("ok", true);
        }

        @GetMapping("/no-encontrado")
        void noEncontrado() {
            throw new RecursoNoEncontradoException("Producto de prueba no encontrado.");
        }

        @GetMapping("/conflicto")
        void conflicto() {
            throw new ConflictoNegocioException("Conflicto de prueba.");
        }

        @GetMapping("/error-interno")
        void errorInterno() {
            throw new RuntimeException("Detalle sensible interno");
        }
    }
}
