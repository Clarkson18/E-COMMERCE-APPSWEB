package com.example.E_Commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    // Almacenar el hash BCrypt, nunca la contraseña original.
    @NotBlank
    @Column(nullable = false, length = 100)
    private String contrasenia;

    @Column(length = 20)
    private String telefono;

    @Column(length = 500)
    private String direccion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol = RolUsuario.CLIENTE;

    @Column(nullable = false)
    private boolean activo = true;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
