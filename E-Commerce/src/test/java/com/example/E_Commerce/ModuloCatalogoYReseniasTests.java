package com.example.E_Commerce;

import com.example.E_Commerce.dto.CrearReseniaRequest;
import com.example.E_Commerce.model.Producto;
import com.example.E_Commerce.model.Resenia;
import com.example.E_Commerce.model.RolUsuario;
import com.example.E_Commerce.model.Usuario;
import com.example.E_Commerce.repository.ProductoRepository;
import com.example.E_Commerce.repository.ReseniaRepository;
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
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ModuloCatalogoYReseniasTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ProductoRepository productos;

    @Autowired
    private ReseniaRepository resenias;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void limpiarAntes() {
        resenias.deleteAll();
        productos.deleteAll();
        usuarios.deleteAll();
    }

    @AfterEach
    void limpiarDespues() {
        resenias.deleteAll();
        productos.deleteAll();
        usuarios.deleteAll();
    }

    private Producto crearProducto(String nombre, String descripcion, String precio, int stock, boolean activo) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setImagenUrl("/imgs/sonywh5.jpeg");
        p.setActivo(activo);
        return productos.save(p);
    }

    private Usuario crearUsuario(String nombre, String email, RolUsuario rol, boolean activo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setTelefono("6441234567");
        u.setDireccion("Av. Kino 205");
        u.setContrasenia(passwordEncoder.encode("ClavePrueba123"));
        u.setRol(rol);
        u.setActivo(activo);
        return usuarios.save(u);
    }

    private Resenia crearResenia(Usuario usuario, Producto producto, int calificacion, String comentario, LocalDateTime fecha) {
        Resenia r = new Resenia();
        r.setUsuario(usuario);
        r.setProducto(producto);
        r.setCalificacion(calificacion);
        r.setComentario(comentario);
        r.setFecha(fecha);
        return resenias.save(r);
    }

    @Test
    void catalogoListaSoloActivosFiltraYOrdenaSeguramente() throws Exception {
        Producto audifonos = crearProducto("Sony WH-1000XM5", "Cancelación de ruido", "4953.00", 12, true);
        Producto laptop = crearProducto("Laptop ZenBook 14", "Ultrabook OLED", "1150.00", 0, true);
        Producto monitor = crearProducto("Monitor LG UltraWide", "Monitor 34 pulgadas", "450.00", 5, true);
        crearProducto("Producto Oculto", "Inactivo", "9999.00", 10, false);

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(audifonos.getId()))
                .andExpect(jsonPath("$[0].disponible").value(true))
                .andExpect(jsonPath("$[1].id").value(laptop.getId()))
                .andExpect(jsonPath("$[1].stock").value(0))
                .andExpect(jsonPath("$[1].disponible").value(false))
                .andExpect(jsonPath("$[2].id").value(monitor.getId()))
                .andExpect(content().string(not(containsString("Producto Oculto"))));

        mvc.perform(get("/api/productos").param("nombre", "sony"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Sony WH-1000XM5"));

        mvc.perform(get("/api/productos").param("nombre", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get("/api/productos")
                        .param("precioMaximo", "1150.00")
                        .param("orden", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(monitor.getId()))
                .andExpect(jsonPath("$[1].id").value(laptop.getId()));

        mvc.perform(get("/api/productos")
                        .param("precioMaximo", "1150.00")
                        .param("orden", "Menor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(monitor.getId()))
                .andExpect(jsonPath("$[1].id").value(laptop.getId()));

        mvc.perform(get("/api/productos").param("precioMaximo", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erroresValidacion.precioMaximo").isNotEmpty());

        mvc.perform(get("/api/productos").param("precioMaximo", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mvc.perform(get("/api/productos").param("orden", "invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString("orden")));
    }

    @Test
    void detalleProductoCalculaPromedioYDevuelve404EnInactivoOInexistente() throws Exception {
        Producto producto = crearProducto("Escáner OBD2", "Diagnóstico automotriz", "699.00", 8, true);
        Producto inactivo = crearProducto("Escáner Antiguo", "Descontinuado", "399.00", 3, false);

        mvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.nombre").value("Escáner OBD2"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.calificacionPromedio").value(0.0))
                .andExpect(jsonPath("$.totalResenias").value(0));

        Usuario cliente = crearUsuario("Pedro Esquer", "pedro@example.com", RolUsuario.CLIENTE, true);
        crearResenia(cliente, producto, 5, "Excelente", LocalDateTime.now().minusDays(2));
        crearResenia(cliente, producto, 4, "Muy bueno", LocalDateTime.now().minusDays(1));
        crearResenia(cliente, producto, 4, "Cumple", LocalDateTime.now());

        mvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calificacionPromedio").value(4.3))
                .andExpect(jsonPath("$.totalResenias").value(3));

        mvc.perform(get("/api/productos/{id}", inactivo.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mvc.perform(get("/api/productos/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void consultaReseniasEsPublicaExponeAutorSinDatosSensiblesYValidaProducto() throws Exception {
        Producto producto = crearProducto("Kit Focos LED", "18000lm", "392.00", 20, true);
        Producto inactivo = crearProducto("Kit Descontinuado", "Inactivo", "250.00", 2, false);
        Usuario autor = crearUsuario("Gael Guerra", "gael.privado@example.com", RolUsuario.CLIENTE, true);

        crearResenia(autor, producto, 4, "Primera opinión", LocalDateTime.now().minusHours(5));
        crearResenia(autor, producto, 5, "Segunda opinión más reciente", LocalDateTime.now());

        mvc.perform(get("/api/productos/{id}/resenias", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productoId").value(producto.getId()))
                .andExpect(jsonPath("$[0].autorNombre").value("Gael Guerra"))
                .andExpect(jsonPath("$[0].calificacion").value(5))
                .andExpect(jsonPath("$[0].comentario").value("Segunda opinión más reciente"))
                .andExpect(jsonPath("$[1].comentario").value("Primera opinión"))
                .andExpect(content().string(not(containsString("gael.privado@example.com"))))
                .andExpect(content().string(not(containsString("contrasenia"))));

        mvc.perform(get("/api/productos/{id}/resenias", inactivo.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mvc.perform(get("/api/productos/{id}/resenias", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void creacionReseniaExigeJwtClienteActivoYValidaEntrada() throws Exception {
        Producto producto = crearProducto("Sony WH-1000XM5", "Audífonos", "4953.00", 10, true);
        Producto inactivo = crearProducto("Oculto", "No visible", "100.00", 5, false);

        Usuario clienteActivo = crearUsuario("Hassan Kabande", "hassan@example.com", RolUsuario.CLIENTE, true);
        Usuario clienteInactivo = crearUsuario("Inactivo", "inactivo@example.com", RolUsuario.CLIENTE, false);
        Usuario admin = crearUsuario("Admin", "admin@example.com", RolUsuario.ADMIN, true);

        String tokenCliente = jwtService.generarToken(clienteActivo);
        String tokenInactivo = jwtService.generarToken(clienteInactivo);
        String tokenAdmin = jwtService.generarToken(admin);

        CrearReseniaRequest valida = new CrearReseniaRequest(5, "  Excelente sonido y comodidad.  ");

        mvc.perform(post("/api/productos/{id}/resenias", producto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(valida)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mvc.perform(post("/api/productos/{id}/resenias", producto.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(valida)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mvc.perform(post("/api/productos/{id}/resenias", producto.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenInactivo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(valida)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        CrearReseniaRequest invalida = new CrearReseniaRequest(6, "   ");
        mvc.perform(post("/api/productos/{id}/resenias", producto.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erroresValidacion.calificacion").isNotEmpty())
                .andExpect(jsonPath("$.erroresValidacion.comentario").isNotEmpty());

        mvc.perform(post("/api/productos/{id}/resenias", inactivo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(valida)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mvc.perform(post("/api/productos/{id}/resenias", 999999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(valida)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mvc.perform(post("/api/productos/{id}/resenias", producto.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(valida)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productoId").value(producto.getId()))
                .andExpect(jsonPath("$.autorNombre").value("Hassan Kabande"))
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$.comentario").value("Excelente sonido y comodidad."))
                .andExpect(jsonPath("$.fecha").isNotEmpty())
                .andExpect(content().string(not(containsString("hassan@example.com"))));

        assertEquals(1, resenias.countByProductoId(producto.getId()));

        mvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calificacionPromedio").value(5.0))
                .andExpect(jsonPath("$.totalResenias").value(1));
    }
}
