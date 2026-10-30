package com.example.E_Commerce_Admin.repository;

import com.example.E_Commerce.model.Resenia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReseniaRepository extends JpaRepository<Resenia, Long> {
    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Resenia> findAllByOrderByFechaDesc();
}
