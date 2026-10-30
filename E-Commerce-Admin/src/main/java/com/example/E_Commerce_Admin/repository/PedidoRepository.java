package com.example.E_Commerce_Admin.repository;

import com.example.E_Commerce.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findAllByOrderByFechaDesc();
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Pedido> findWithDetallesById(Long id);
}
