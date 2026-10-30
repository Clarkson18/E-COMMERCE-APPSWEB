package com.example.E_Commerce_Admin.controllers;

import com.example.E_Commerce.model.Producto;
import com.example.E_Commerce_Admin.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {
    private final ProductoService productos;

    public ProductoController(ProductoService productos) { this.productos = productos; }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar, Model model) {
        model.addAttribute("productos", productos.listar(buscar));
        model.addAttribute("buscar", buscar);
        return "admin/productos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "admin/producto-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productos.obtener(id));
        model.addAttribute("id", id);
        return "admin/producto-form";
    }

    @PostMapping
    public String crear(@RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) BigDecimal precio,
            @RequestParam(required = false) Integer stock,
            @RequestParam(defaultValue = "") String imagenUrl,
            @RequestParam(defaultValue = "false") boolean activo,
            Model model, RedirectAttributes flash) {
        Producto producto = datosProducto(nombre, descripcion, precio, stock, imagenUrl, activo);
        if (mostrarErrores(producto, model)) return "admin/producto-form";
        productos.guardar(null, producto);
        flash.addFlashAttribute("mensaje", "Producto creado.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) BigDecimal precio,
            @RequestParam(required = false) Integer stock,
            @RequestParam(defaultValue = "") String imagenUrl,
            @RequestParam(defaultValue = "false") boolean activo,
            Model model, RedirectAttributes flash) {
        Producto producto = datosProducto(nombre, descripcion, precio, stock, imagenUrl, activo);
        model.addAttribute("id", id);
        if (mostrarErrores(producto, model)) return "admin/producto-form";
        productos.guardar(id, producto);
        flash.addFlashAttribute("mensaje", "Producto actualizado.");
        return "redirect:/admin/productos";
    }

    // Igual que en GameVault: recibir parámetros y armar la entidad con setters.
    private Producto datosProducto(String nombre, String descripcion, BigDecimal precio,
            Integer stock, String imagenUrl, boolean activo) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setImagenUrl(imagenUrl);
        producto.setActivo(activo);
        return producto;
    }

    // El servicio valida; el controlador solamente presenta los errores en Thymeleaf.
    private boolean mostrarErrores(Producto producto, Model model) {
        var errores = new BeanPropertyBindingResult(producto, "producto");
        productos.validar(producto).forEach((campo, mensaje) ->
                errores.rejectValue(campo, "invalido", mensaje));
        model.addAllAttributes(errores.getModel());
        return errores.hasErrors();
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, RedirectAttributes flash) {
        productos.cambiarEstado(id);
        flash.addFlashAttribute("mensaje", "Estado del producto actualizado.");
        return "redirect:/admin/productos";
    }
}
