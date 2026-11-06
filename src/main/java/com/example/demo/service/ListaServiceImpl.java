package com.example.demo.service;

import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.dto.ListaRequestDTO;
import com.example.demo.dto.ListaResponseDTO;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaConFavoritosException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListaServiceImpl {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaServiceImpl(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    public ListaResponseDTO crear(ListaRequestDTO dto) {
        AuthenticatedUser currentUser = obtenerUsuarioAutenticado();
        Lista lista = new Lista();
        lista.setNombre(dto.nombre());
        lista.setUsuarioId(currentUser.id());
        Lista guardado = listaRepository.save(lista);
        return mapearAResponse(guardado);
    }

    public List<ListaResponseDTO> listarTodos() {
        AuthenticatedUser currentUser = obtenerUsuarioAutenticado();
        List<Lista> listas = currentUser.isAdmin()
                ? listaRepository.findAll()
                : listaRepository.findByUsuarioId(currentUser.id());

        return listas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public ListaResponseDTO obtenerPorId(Long id) {
        Lista lista = obtenerListaValidandoAcceso(id);
        return mapearAResponse(lista);
    }

    public List<FavoritoResponseDTO> listarFavoritosDeLista(Long listaId) {
        obtenerListaValidandoAcceso(listaId);
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
        obtenerListaValidandoAcceso(id);

        List<Favorito> favoritos = favoritoRepository.findByListaId(id);
        if (!favoritos.isEmpty()) {
            throw new ListaConFavoritosException("La lista contiene favoritos y no puede ser eliminada");
        }

        listaRepository.deleteById(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        obtenerListaValidandoAcceso(origenId);
        obtenerListaValidandoAcceso(destinoId);

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito fav : favoritos) {
            fav.setListaId(destinoId);
            favoritoRepository.save(fav);
        }

        listaRepository.deleteById(origenId);
    }

    public Lista obtenerListaValidandoAcceso(Long listaId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada"));

        AuthenticatedUser currentUser = obtenerUsuarioAutenticado();
        if (!currentUser.isAdmin() && (lista.getUsuarioId() == null || !lista.getUsuarioId().equals(currentUser.id()))) {
            throw new AccesoDenegadoException("No tenés permiso para operar sobre una lista ajena");
        }
        return lista;
    }

    protected AuthenticatedUser obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new AccesoDenegadoException("Usuario no autenticado");
    }

    private ListaResponseDTO mapearAResponse(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre());
    }
}
