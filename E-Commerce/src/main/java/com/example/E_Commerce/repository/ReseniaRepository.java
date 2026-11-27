package com.example.E_Commerce.repository;

import com.example.E_Commerce.model.Resenia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReseniaRepository extends JpaRepository<Resenia, Long> {

    @EntityGraph(attributePaths = { "usuario" })
    List<Resenia> findByProductoIdOrderByFechaDescIdDesc(Long productoId);

    long countByProductoId(Long productoId);

    @Query("SELECT COALESCE(AVG(r.calificacion), 0.0) FROM Resenia r WHERE r.producto.id = :productoId")
    Double obtenerPromedioPorProductoId(@Param("productoId") Long productoId);
}
