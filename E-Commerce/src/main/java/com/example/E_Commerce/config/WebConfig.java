package com.example.E_Commerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirecciones y mapeos de vistas para Thymeleaf
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/index.html").setViewName("index");

        registry.addViewController("/catalogo").setViewName("catalogo");
        registry.addViewController("/catalogo.html").setViewName("catalogo");

        registry.addViewController("/carrito").setViewName("carrito");
        registry.addViewController("/carrito.html").setViewName("carrito");

        registry.addViewController("/pedidos").setViewName("pedidos");
        registry.addViewController("/pedidos.html").setViewName("pedidos");

        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/login.html").setViewName("login");

        registry.addViewController("/register").setViewName("register");
        registry.addViewController("/register.html").setViewName("register");

        registry.addViewController("/perfilUsuario").setViewName("perfilUsuario");
        registry.addViewController("/perfilUsuario.html").setViewName("perfilUsuario");

        registry.addViewController("/detallesProducto").setViewName("detallesProducto");
        registry.addViewController("/detallesProducto.html").setViewName("detallesProducto");

        registry.addViewController("/crearResenia").setViewName("crearResenia");
        registry.addViewController("/crearResenia.html").setViewName("crearResenia");

        registry.addViewController("/procesoPago").setViewName("procesoPago");
        registry.addViewController("/procesoPago.html").setViewName("procesoPago");

        registry.addViewController("/pagoConfirmado").setViewName("pagoConfirmado");
        registry.addViewController("/pagoConfirmado.html").setViewName("pagoConfirmado");

        // Rutas del panel de administración
        registry.addViewController("/adminPrincipal").setViewName("adminPrincipal");
        registry.addViewController("/adminPrincipal.html").setViewName("adminPrincipal");

        registry.addViewController("/adminCatalogo").setViewName("adminCatalogo");
        registry.addViewController("/adminCatalogo.html").setViewName("adminCatalogo");

        registry.addViewController("/adminCrearProducto").setViewName("adminCrearProducto");
        registry.addViewController("/adminCrearProducto.html").setViewName("adminCrearProducto");

        registry.addViewController("/adminEditarProducto").setViewName("adminEditarProducto");
        registry.addViewController("/adminEditarProducto.html").setViewName("adminEditarProducto");

        registry.addViewController("/adminGestionPedidos").setViewName("adminGestionPedidos");
        registry.addViewController("/adminGestionPedidos.html").setViewName("adminGestionPedidos");

        registry.addViewController("/adminGestionResenas").setViewName("adminGestionResenas");
        registry.addViewController("/adminGestionResenas.html").setViewName("adminGestionResenas");

        registry.addViewController("/adminUsuarios").setViewName("adminUsuarios");
        registry.addViewController("/adminUsuarios.html").setViewName("adminUsuarios");
    }
}
