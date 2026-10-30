package com.example.E_Commerce_Admin.service;

import com.example.E_Commerce.model.*;
import com.example.E_Commerce_Admin.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PedidoService {
    private final PedidoRepository pedidos;
    public PedidoService(PedidoRepository pedidos) { this.pedidos = pedidos; }
    public List<Pedido> listar() { return pedidos.findAllByOrderByFechaDesc(); }
    public Pedido obtener(Long id) { return pedidos.findWithDetallesById(id).orElseThrow(RecursoNoEncontradoException::new); }
    @Transactional
    public void cambiarEstado(Long id, EstadoPedido estado) {
        var pedido = obtener(id);
        boolean valido = pedido.getEstado() == estado
                || (pedido.getEstado() == EstadoPedido.PENDIENTE && estado == EstadoPedido.ENVIADO)
                || (pedido.getEstado() == EstadoPedido.ENVIADO && estado == EstadoPedido.ENTREGADO);
        if (!valido) throw new IllegalArgumentException("El pedido debe avanzar de Pendiente a Enviado y después a Entregado.");
        pedido.setEstado(estado); pedidos.save(pedido);
    }
}
