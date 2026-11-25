package com.example.E_Commerce;

import com.example.E_Commerce.controller.PerfilRestController;
import com.example.E_Commerce.dto.ActualizarPerfilRequest;
import com.example.E_Commerce.dto.CrearSesionRequest;
import com.example.E_Commerce.dto.RegistroUsuarioRequest;
import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.UsuarioRepository;
import com.example.E_Commerce.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ModuloClientesAuthPerfilTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void limpiarAntes() {
        usuarios.deleteAll();
    }

    @AfterEach
    void limpiarDespues() {
        usuarios.deleteAll();
    }

    private Usuario crearUsuarioEnBd(String nombre, String email, String clavePlana, RolUsuario rol, boolean activo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setTelefono("6441234567");
        u.setDireccion("Calle Prueba 100");
        u.setContrasenia(passwordEncoder.encode(clavePlana));
        u.setRol(rol);
        u.setActivo(activo);
        return usuarios.save(u);
    }

    private String iniciarSesionYObtenerToken(String email, String clave) throws Exception {
        CrearSesionRequest req = new CrearSesionRequest(email, clave);
        MvcResult resultado = mvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = jsonMapper.readTree(resultado.getResponse().getContentAsString());
        return json.get("token").asString();
    }

    @Test
    void registroExitosoGuardaHashBCryptYNoExponeContrasenia() throws Exception {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest(
                "Juan Roaro",
                "+52 644 123 4567",
                "Juan.Roaro@Example.com",
                "Av. Kino y Antonio Caso #205",
                "ClaveSegura123",
                "ClaveSegura123");

        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Juan Roaro"))
                .andExpect(jsonPath("$.email").value("juan.roaro@example.com"))
                .andExpect(jsonPath("$.telefono").value("+52 644 123 4567"))
                .andExpect(jsonPath("$.direccion").value("Av. Kino y Antonio Caso #205"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.fechaRegistro").isNotEmpty())
                .andExpect(jsonPath("$.tokenRenovado").doesNotExist())
                .andExpect(content().string(not(containsString("contrasenia"))))
                .andExpect(content().string(not(containsString("ClaveSegura123"))));

        Usuario guardado = usuarios.findByEmailIgnoreCase("juan.roaro@example.com").orElseThrow();
        assertEquals(RolUsuario.CLIENTE, guardado.getRol());
        assertTrue(guardado.isActivo());
        assertNotEquals("ClaveSegura123", guardado.getContrasenia());
        assertTrue(passwordEncoder.matches("ClaveSegura123", guardado.getContrasenia()));
    }

    @Test
    void registroRechazaDatosInvalidosConfirmacionDistintaYEmailDuplicado() throws Exception {
        RegistroUsuarioRequest invalido = new RegistroUsuarioRequest(
                " ",
                "abc",
                "correo-invalido",
                "x".repeat(501),
                "123",
                "");

        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erroresValidacion.nombre").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.telefono").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.email").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.direccion").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.contrasenia").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.confirmacionContrasenia").isNotEmpty());

        RegistroUsuarioRequest distinta = new RegistroUsuarioRequest(
                "Ana Pérez",
                "6441112233",
                "ana@example.com",
                "Calle 5 de Febrero 400",
                "ClaveSegura123",
                "OtraClave123");

        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(distinta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString("coinciden")));

        crearUsuarioEnBd("Existente", "duplicado@example.com", "ClaveSegura123", RolUsuario.CLIENTE, true);

        RegistroUsuarioRequest duplicado = new RegistroUsuarioRequest(
                "Otro Usuario",
                "6449998877",
                "DUPLICADO@EXAMPLE.COM",
                "Dirección 2",
                "ClaveSegura123",
                "ClaveSegura123");

        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void inicioSesionEmiteJwtValidoYRechazaInvalidosAdminEInactivos() throws Exception {
        crearUsuarioEnBd("Cliente Activo", "cliente@example.com", "ClaveCliente123", RolUsuario.CLIENTE, true);
        crearUsuarioEnBd("Cliente Inactivo", "inactivo@example.com", "ClaveCliente123", RolUsuario.CLIENTE, false);
        crearUsuarioEnBd("Administrador", "admin@example.com", "ClaveAdmin1234", RolUsuario.ADMIN, true);

        CrearSesionRequest loginValido = new CrearSesionRequest("CLIENTE@EXAMPLE.COM", "ClaveCliente123");
        MvcResult resultado = mvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEnMs").isNumber())
                .andExpect(jsonPath("$.usuario.email").value("cliente@example.com"))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(content().string(not(containsString("contrasenia"))))
                .andReturn();

        String token = jsonMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asString();
        assertTrue(jwtService.esTokenValido(token));

        mvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CrearSesionRequest("cliente@example.com", "Incorrecta999"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CrearSesionRequest("noexiste@example.com", "ClaveCliente123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CrearSesionRequest("admin@example.com", "ClaveAdmin1234"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CrearSesionRequest("inactivo@example.com", "ClaveCliente123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void perfilConsultaActualizaYRenuevaTokenAlCambiarEmail() throws Exception {
        crearUsuarioEnBd("Cliente Perfil", "perfil@example.com", "ClavePerfil123", RolUsuario.CLIENTE, true);
        crearUsuarioEnBd("Otro Cliente", "otro@example.com", "ClavePerfil123", RolUsuario.CLIENTE, true);

        mvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized());

        String tokenOriginal = iniciarSesionYObtenerToken("perfil@example.com", "ClavePerfil123");

        mvc.perform(get("/api/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOriginal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cliente Perfil"))
                .andExpect(jsonPath("$.email").value("perfil@example.com"))
                .andExpect(jsonPath("$.tokenRenovado").doesNotExist())
                .andExpect(content().string(not(containsString("contrasenia"))));

        ActualizarPerfilRequest mismoEmail = new ActualizarPerfilRequest(
                "Cliente Actualizado",
                "6447778899",
                "perfil@example.com",
                "Nueva Calle 222");

        mvc.perform(put("/api/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOriginal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(mismoEmail)))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(PerfilRestController.HEADER_NUEVO_TOKEN))
                .andExpect(jsonPath("$.nombre").value("Cliente Actualizado"))
                .andExpect(jsonPath("$.telefono").value("6447778899"))
                .andExpect(jsonPath("$.direccion").value("Nueva Calle 222"))
                .andExpect(jsonPath("$.tokenRenovado").doesNotExist());

        ActualizarPerfilRequest emailTomado = new ActualizarPerfilRequest(
                "Cliente Actualizado",
                "6447778899",
                "OTRO@EXAMPLE.COM",
                "Nueva Calle 222");

        mvc.perform(put("/api/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOriginal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(emailTomado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        ActualizarPerfilRequest nuevoEmailReq = new ActualizarPerfilRequest(
                "Cliente Nuevo Correo",
                "6440001122",
                "nuevo.correo@example.com",
                "Av. Reforma 99");

        MvcResult cambioResultado = mvc.perform(put("/api/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOriginal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(nuevoEmailReq)))
                .andExpect(status().isOk())
                .andExpect(header().exists(PerfilRestController.HEADER_NUEVO_TOKEN))
                .andExpect(jsonPath("$.email").value("nuevo.correo@example.com"))
                .andExpect(jsonPath("$.tokenRenovado").isNotEmpty())
                .andReturn();

        String headerToken = cambioResultado.getResponse().getHeader(PerfilRestController.HEADER_NUEVO_TOKEN);
        String bodyToken = jsonMapper.readTree(cambioResultado.getResponse().getContentAsString())
                .get("tokenRenovado").asString();
        assertNotNull(headerToken);
        assertEquals(headerToken, bodyToken);
        assertEquals("nuevo.correo@example.com", jwtService.extraerUsuario(headerToken).email());

        mvc.perform(get("/api/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo.correo@example.com"));

        mvc.perform(get("/api/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOriginal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo.correo@example.com"));
    }
}
