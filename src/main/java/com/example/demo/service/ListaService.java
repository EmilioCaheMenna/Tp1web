package com.example.demo.service;

import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.dto.ListaRequestDTO;
import com.example.demo.dto.ListaResponseDTO;
import com.example.demo.exception.ListaConFavoritosException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaService(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada"));
    }

    public List<FavoritoResponseDTO> listarFavoritosDeLista(Long listaId) {
        if (!listaRepository.existsById(listaId)) {
            throw new RecursoNoEncontradoException("Lista no encontrada");
        }
        return favoritoRepository.findByListaId(listaId).stream()
                .map(fav -> new FavoritoResponseDTO(
                        fav.getId(),
                        fav.getProductoId(),
                        fav.getListaId(),
                        fav.getNotaPersonal(),
                        fav.getFechaAgregado()
                ))
                .collect(Collectors.toList());
    }

    public void eliminar(Long id) {
        if (!listaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Lista no encontrada");
        }
        
        List<Favorito> favoritos = favoritoRepository.findByListaId(id);
        if (!favoritos.isEmpty()) {
            throw new ListaConFavoritosException("La lista contiene favoritos y no puede ser eliminada");
        }
        
        listaRepository.deleteById(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        if (!listaRepository.existsById(origenId)) {
            throw new RecursoNoEncontradoException("Lista origen no encontrada");
        }
        if (!listaRepository.existsById(destinoId)) {
            throw new RecursoNoEncontradoException("Lista destino no encontrada");
        }

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito fav : favoritos) {
            fav.setListaId(destinoId);
            favoritoRepository.save(fav);
        }

        listaRepository.deleteById(origenId);
    }

    private ListaResponseDTO mapearAResponse(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre());
    }
}
