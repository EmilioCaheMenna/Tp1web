package com.example.demo.service;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoService {

    private final FavoritoRepository repository;

    public FavoritoService(FavoritoRepository repository) {
        this.repository = repository;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO dto) {
        Favorito favorito = new Favorito();
        favorito.setProductoId(dto.productoId());
        favorito.setNotaPersonal(dto.notaPersonal());
        favorito.setFechaAgregado(LocalDateTime.now());

        Favorito guardado = repository.save(favorito);
        return mapearAResponse(guardado);
    }

    public List<FavoritoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::mapearAResponse)
                .orElseThrow(() -> new RuntimeException("Favorito no encontrado"));
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO dto) {
        Favorito existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorito no encontrado"));

        existente.setProductoId(dto.productoId());
        existente.setNotaPersonal(dto.notaPersonal());

        Favorito actualizado = repository.save(existente);
        return mapearAResponse(actualizado);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Favorito no encontrado");
        }
        repository.deleteById(id);
    }

    private FavoritoResponseDTO mapearAResponse(Favorito favorito) {
        return new FavoritoResponseDTO(
            favorito.getId(),
            favorito.getProductoId(),
            favorito.getNotaPersonal(),
            favorito.getFechaAgregado()
        );
    }
}