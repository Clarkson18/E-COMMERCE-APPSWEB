package com.example.E_Commerce_Admin.controllers;

import com.example.E_Commerce_Admin.service.ReseniaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/resenias")
public class ReseniaController {
    private final ReseniaService resenias;
    public ReseniaController(ReseniaService resenias) { this.resenias = resenias; }
    @GetMapping public String listar(Model model) {
        model.addAttribute("resenias", resenias.listar()); return "admin/resenias";
    }
    @GetMapping("/{id}/eliminar") public String confirmar(@PathVariable Long id, Model model) {
        model.addAttribute("resenia", resenias.obtener(id)); return "admin/resenia-eliminar";
    }
    @PostMapping("/{id}/eliminar") public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        resenias.eliminar(id); flash.addFlashAttribute("mensaje", "Reseña eliminada.");
        return "redirect:/admin/resenias";
    }
}
