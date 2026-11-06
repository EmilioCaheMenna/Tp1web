package com.example.demo.service;

import com.example.demo.dto.ListaRequestDTO;
import com.example.demo.dto.ListaResponseDTO;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoEntity;
import com.example.demo.repository.FavoritoJpaRepository;
import com.example.demo.repository.ListaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoJpaRepository favoritoJpaRepository;

    public ListaService(ListaRepository listaRepository, FavoritoJpaRepository favoritoJpaRepository) {
        this.listaRepository = listaRepository;
        this.favoritoJpaRepository = favoritoJpaRepository;
    }

    public ListaResponseDTO crear(ListaRequestDTO dto) {
        Lista lista = new Lista();
        lista.setNombre(dto.nombre());
        Lista guardado = listaRepository.save(lista);
        return mapearAResponse(guardado);
    }

    public List<ListaResponseDTO> listarTodos() {
        return listaRepository.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public ListaResponseDTO obtenerPorId(Long id) {
        return listaRepository.findById(id)
                .map(this::mapearAResponse)
                .orElseThrow(() -> new com.example.demo.exception.RecursoNoEncontradoException("Lista no encontrada"));
    }

    public void eliminar(Long id) {
        if (!listaRepository.existsById(id)) {
            throw new com.example.demo.exception.RecursoNoEncontradoException("Lista no encontrada");
        }
        
        List<FavoritoEntity> favoritos = favoritoJpaRepository.findByListaId(id);
        if (!favoritos.isEmpty()) {
            throw new com.example.demo.exception.ListaConFavoritosException("La lista contiene favoritos y no puede ser eliminada");
        }
        
        listaRepository.deleteById(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        if (!listaRepository.existsById(origenId)) {
            throw new com.example.demo.exception.RecursoNoEncontradoException("Lista origen no encontrada");
        }
        if (!listaRepository.existsById(destinoId)) {
            throw new com.example.demo.exception.RecursoNoEncontradoException("Lista destino no encontrada");
        }

        List<FavoritoEntity> favoritos = favoritoJpaRepository.findByListaId(origenId);
        com.example.demo.repository.ListaEntity listaDestino = new com.example.demo.repository.ListaEntity();
        listaDestino.setId(destinoId);
        
        for (FavoritoEntity fav : favoritos) {
            fav.setLista(listaDestino);
            favoritoJpaRepository.save(fav);
        }

        listaRepository.deleteById(origenId);
    }

    private ListaResponseDTO mapearAResponse(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre());
    }
}
