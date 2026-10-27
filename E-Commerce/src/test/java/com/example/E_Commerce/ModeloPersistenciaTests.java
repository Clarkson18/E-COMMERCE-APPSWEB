package com.example.E_Commerce;

import com.example.E_Commerce.model.*;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ModeloPersistenciaTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private Validator validator;

    @Test
    void guardaRelacionesYConservaElPrecioDeCompra() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente de prueba");
        usuario.setEmail("cliente@example.com");
        usuario.setContrasenia(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("clave-de-prueba"));
        entityManager.persist(usuario);

        Producto producto = new Producto();
        producto.setNombre("Escáner OBD2");
        producto.setPrecio(new BigDecimal("699.00"));
        producto.setStock(5);
        entityManager.persist(producto);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio("Calle de prueba 123");
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(producto.getPrecio());
        pedido.agregarDetalle(detalle);
        entityManager.persist(pedido);

        Resenia resenia = new Resenia();
        resenia.setUsuario(usuario);
        resenia.setProducto(producto);
        resenia.setCalificacion(5);
        resenia.setComentario("Buen producto");
        entityManager.persist(resenia);
        producto.setPrecio(new BigDecimal("799.00"));
        entityManager.flush();
        entityManager.clear();

        Pedido guardado = entityManager.find(Pedido.class, pedido.getId());
        assertEquals(usuario.getId(), guardado.getUsuario().getId());
        assertEquals(EstadoPedido.PENDIENTE, guardado.getEstado());
        assertEquals(RolUsuario.CLIENTE, guardado.getUsuario().getRol());
        assertEquals(1, guardado.getDetalles().size());
        assertEquals(new BigDecimal("1398.00"), guardado.getTotal());
        assertEquals(new BigDecimal("799.00"), guardado.getDetalles().getFirst().getProducto().getPrecio());
        Resenia guardada = entityManager.find(Resenia.class, resenia.getId());
        assertEquals(producto.getId(), guardada.getProducto().getId());
        assertEquals(usuario.getId(), guardada.getUsuario().getId());

        Long detalleId = guardado.getDetalles().getFirst().getId();
        guardado.eliminarDetalle(guardado.getDetalles().getFirst());
        entityManager.flush();
        entityManager.clear();
        assertNull(entityManager.find(DetallePedido.class, detalleId));
        assertNotNull(entityManager.find(Producto.class, producto.getId()));
        assertNotNull(entityManager.find(Usuario.class, usuario.getId()));
    }

    @Test
    void rechazaValoresFueraDelDominio() {
        Producto producto = new Producto();
        producto.setNombre("Producto");
        producto.setPrecio(new BigDecimal("-1.00"));
        producto.setStock(-1);
        var errores = validator.validate(producto);
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("precio")));
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("stock")));

        Resenia resenia = new Resenia();
        resenia.setCalificacion(6);
        assertFalse(validator.validateProperty(resenia, "calificacion").isEmpty());
        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(0);
        assertFalse(validator.validateProperty(detalle, "cantidad").isEmpty());
    }
}
