package com.example.E_Commerce_Admin.controllers;

import com.example.E_Commerce_Admin.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {
    private final UsuarioService usuarios;
    public UsuarioController(UsuarioService usuarios) { this.usuarios = usuarios; }
    @GetMapping public String listar(Model model) {
        model.addAttribute("usuarios", usuarios.listar()); return "admin/usuarios";
    }
    @PostMapping("/{id}/estado") public String estado(@PathVariable Long id, RedirectAttributes flash) {
        try { usuarios.cambiarEstado(id); flash.addFlashAttribute("mensaje", "Estado del usuario actualizado."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/usuarios";
    }
}
