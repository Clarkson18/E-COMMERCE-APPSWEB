package com.example.E_Commerce_Admin.controllers;

import com.example.E_Commerce.model.EstadoPedido;
import com.example.E_Commerce_Admin.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {
    private final PedidoService pedidos;
    public PedidoController(PedidoService pedidos) { this.pedidos = pedidos; }
    @GetMapping public String listar(Model model) {
        model.addAttribute("pedidos", pedidos.listar()); return "admin/pedidos";
    }
    @GetMapping("/{id}") public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidos.obtener(id)); model.addAttribute("estados", EstadoPedido.values());
        return "admin/pedido";
    }
    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam EstadoPedido estado, RedirectAttributes flash) {
        try { pedidos.cambiarEstado(id, estado); flash.addFlashAttribute("mensaje", "Estado actualizado."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/pedidos/" + id;
    }
}
