package com.example.E_Commerce_Admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/") public String inicio() { return "redirect:/admin"; }
    @GetMapping({"/admin", "/admin/"}) public String panel() { return "admin/inicio"; }
    @GetMapping("/admin/login") public String login() { return "admin/login"; }
}
