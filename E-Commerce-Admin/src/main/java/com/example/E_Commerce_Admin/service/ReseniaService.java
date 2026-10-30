package com.example.E_Commerce_Admin.service;

import com.example.E_Commerce.model.Resenia;
import com.example.E_Commerce_Admin.repository.ReseniaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReseniaService {
    private final ReseniaRepository resenias;
    public ReseniaService(ReseniaRepository resenias) { this.resenias = resenias; }
    public List<Resenia> listar() { return resenias.findAllByOrderByFechaDesc(); }
    public Resenia obtener(Long id) { return resenias.findById(id).orElseThrow(RecursoNoEncontradoException::new); }
    @Transactional
    public void eliminar(Long id) { resenias.delete(obtener(id)); }
}
