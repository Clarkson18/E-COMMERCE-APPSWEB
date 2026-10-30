package com.example.E_Commerce_Admin;

import com.example.E_Commerce.model.*;
import com.example.E_Commerce_Admin.repository.*;
import com.example.E_Commerce_Admin.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdministracionTests {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ProductoRepository productos;
    @Autowired PedidoRepository pedidos;
    @Autowired ReseniaRepository resenias;
    @Autowired PasswordEncoder encoder;
    Usuario admin;
    Usuario cliente;

    @BeforeEach void preparar() {
        resenias.deleteAll(); pedidos.deleteAll(); productos.deleteAll(); usuarios.deleteAll();
        admin = crearUsuario("admin@example.com", RolUsuario.ADMIN);
        cliente = crearUsuario("cliente@example.com", RolUsuario.CLIENTE);
    }
    Usuario crearUsuario(String email, RolUsuario rol) {
        var u = new Usuario(); u.setEmail(email); u.setNombre("Prueba"); u.setRol(rol);
        u.setContrasenia(encoder.encode("ClaveSoloPruebas123")); return usuarios.save(u);
    }
    Producto crearProducto() {
        var p = new Producto(); p.setNombre("Producto de prueba"); p.setPrecio(new BigDecimal("50.00")); p.setStock(3);
        return productos.save(p);
    }
    MockHttpSession ingresar() throws Exception {
        return (MockHttpSession) mvc.perform(post("/admin/login").with(csrf())
                .param("email", admin.getEmail()).param("password", "ClaveSoloPruebas123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated()).andReturn().getRequest().getSession(false);
    }
    @Test void accesoSesionYCierre() throws Exception {
        mvc.perform(get("/admin/login")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("_csrf")));
        for (String ruta : new String[]{"/admin", "/admin/productos", "/admin/pedidos", "/admin/usuarios", "/admin/resenias"})
            mvc.perform(get(ruta)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/login"));
        var session = ingresar();
        for (String ruta : new String[]{"/admin", "/admin/productos", "/admin/productos/nuevo", "/admin/pedidos", "/admin/usuarios", "/admin/resenias"})
            mvc.perform(get(ruta).session(session)).andExpect(status().isOk());
        mvc.perform(post("/admin/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/logout").session(session).with(csrf())).andExpect(redirectedUrl("/admin/login?logout")).andExpect(unauthenticated());
        assertTrue(session.isInvalid());
    }
    @Test void rechazaCredencialesClientesEInactivos() throws Exception {
        mvc.perform(post("/admin/login").with(csrf()).param("email", admin.getEmail()).param("password", "incorrecta"))
                .andExpect(redirectedUrl("/admin/login?error")).andExpect(unauthenticated());
        mvc.perform(post("/admin/login").with(csrf()).param("email", cliente.getEmail()).param("password", "ClaveSoloPruebas123"))
                .andExpect(redirectedUrl("/admin/login?error")).andExpect(unauthenticated());
        mvc.perform(get("/admin/productos").with(user(cliente.getEmail()).roles("CLIENTE"))).andExpect(status().isForbidden());
        var session = ingresar();
        admin.setActivo(false); usuarios.save(admin);
        mvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/login").with(csrf()).param("email", admin.getEmail()).param("password", "ClaveSoloPruebas123"))
                .andExpect(redirectedUrl("/admin/login?error"));
    }
    @Test void productosValidacionCsrfYEdicion() throws Exception {
        var session = ingresar();
        mvc.perform(post("/admin/productos").session(session).param("nombre", "Sin token").param("precio", "10").param("stock", "1"))
                .andExpect(status().isForbidden()); assertEquals(0, productos.count());
        mvc.perform(post("/admin/productos").session(session).with(csrf()).param("nombre", "").param("precio", "-1").param("stock", "-2"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("producto", "nombre", "precio", "stock"));
        assertEquals(0, productos.count());
        mvc.perform(post("/admin/productos").session(session).with(csrf()).param("nombre", "Imagen inválida")
                .param("precio", "10").param("stock", "1").param("imagenUrl", "javascript:alert(1)"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("producto", "imagenUrl"));
        mvc.perform(post("/admin/productos").session(session).with(csrf()).param("nombre", "Nuevo").param("precio", "20.50")
                .param("stock", "3").param("activo", "true").param("imagenUrl", "https://example.com/imagen.png"))
                .andExpect(redirectedUrl("/admin/productos"));
        var p = productos.findAll().getFirst();
        mvc.perform(get("/admin/productos/" + p.getId() + "/editar").session(session)).andExpect(status().isOk());
        mvc.perform(post("/admin/productos/" + p.getId() + "/editar").session(session).with(csrf())
                .param("nombre", "Editado").param("precio", "30.00").param("stock", "4").param("activo", "true"))
                .andExpect(redirectedUrl("/admin/productos"));
        assertEquals("Editado", productos.findById(p.getId()).orElseThrow().getNombre());
        mvc.perform(post("/admin/productos/" + p.getId() + "/estado").session(session).with(csrf())).andExpect(status().is3xxRedirection());
        assertFalse(productos.findById(p.getId()).orElseThrow().isActivo());
        mvc.perform(get("/admin/productos/999999/editar").session(session)).andExpect(status().isNotFound());
    }
    @Test void pedidosConservanPrecioYValidanTransiciones() throws Exception {
        var p = crearProducto();
        var pedido = new Pedido(); pedido.setUsuario(cliente); pedido.setDireccionEnvio("Dirección de prueba");
        var d = new DetallePedido(); d.setProducto(p); d.setCantidad(2); d.setPrecioUnitario(p.getPrecio()); pedido.agregarDetalle(d);
        pedido = pedidos.save(pedido); p.setPrecio(new BigDecimal("99.00")); productos.save(p);
        var session = ingresar(); String ruta = "/admin/pedidos/" + pedido.getId();
        mvc.perform(get("/admin/pedidos").session(session)).andExpect(status().isOk());
        mvc.perform(get(ruta).session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("100.00")));
        mvc.perform(post(ruta + "/estado").session(session).with(csrf()).param("estado", "ENTREGADO"))
                .andExpect(redirectedUrl(ruta)).andExpect(flash().attributeExists("error"));
        assertEquals(EstadoPedido.PENDIENTE, pedidos.findById(pedido.getId()).orElseThrow().getEstado());
        for (String estado : new String[]{"ENVIADO", "ENTREGADO"})
            mvc.perform(post(ruta + "/estado").session(session).with(csrf()).param("estado", estado)).andExpect(redirectedUrl(ruta));
        assertEquals(EstadoPedido.ENTREGADO, pedidos.findById(pedido.getId()).orElseThrow().getEstado());
    }
    @Test void usuariosYModeracion() throws Exception {
        var r = new Resenia(); r.setUsuario(cliente); r.setProducto(crearProducto()); r.setCalificacion(4); r.setComentario("Comentario de prueba");
        r = resenias.save(r); var session = ingresar();
        mvc.perform(get("/admin/resenias").session(session)).andExpect(status().isOk());
        mvc.perform(get("/admin/resenias/" + r.getId() + "/eliminar").session(session)).andExpect(status().isOk());
        assertTrue(resenias.existsById(r.getId()));
        mvc.perform(post("/admin/resenias/" + r.getId() + "/eliminar").session(session).with(csrf())).andExpect(redirectedUrl("/admin/resenias"));
        assertFalse(resenias.existsById(r.getId()));
        mvc.perform(post("/admin/usuarios/" + cliente.getId() + "/estado").session(session).with(csrf())).andExpect(redirectedUrl("/admin/usuarios"));
        assertFalse(usuarios.findById(cliente.getId()).orElseThrow().isActivo());
        mvc.perform(post("/admin/usuarios/" + admin.getId() + "/estado").session(session).with(csrf())).andExpect(flash().attributeExists("error"));
        assertTrue(usuarios.findById(admin.getId()).orElseThrow().isActivo());
    }
}
