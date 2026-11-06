package com.example.demo.service;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoService {

    private final FavoritoRepository repository;
    private final ListaRepository listaRepository;

    public FavoritoService(FavoritoRepository repository, ListaRepository listaRepository) {
        this.repository = repository;
        this.listaRepository = listaRepository;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO dto) {
        validarAccesoALista(dto.listaId());
        Favorito favorito = new Favorito();
        favorito.setProductoId(dto.productoId());
        favorito.setListaId(dto.listaId());
        favorito.setNotaPersonal(dto.notaPersonal());
        favorito.setFechaAgregado(LocalDateTime.now());

        Favorito guardado = repository.save(favorito);
        return mapearAResponse(guardado);
    }

    public List<FavoritoResponseDTO> listarTodos() {
        AuthenticatedUser user = obtenerUsuarioAutenticado();
        if (user == null || user.isAdmin()) {
            return repository.findAll().stream()
                    .map(this::mapearAResponse)
                    .collect(Collectors.toList());
        }
        List<Long> misListasIds = listaRepository.findByUsuarioId(user.id()).stream()
                .map(Lista::getId)
                .toList();

        return repository.findAll().stream()
                .filter(f -> misListasIds.contains(f.getListaId()))
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        Favorito favorito = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado"));
        validarAccesoALista(favorito.getListaId());
        return mapearAResponse(favorito);
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO dto) {
        Favorito existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado"));
        validarAccesoALista(existente.getListaId());
        validarAccesoALista(dto.listaId());

        existente.setProductoId(dto.productoId());
        existente.setListaId(dto.listaId());
        existente.setNotaPersonal(dto.notaPersonal());

        Favorito actualizado = repository.save(existente);
        return mapearAResponse(actualizado);
    }

    public void eliminar(Long id) {
        Favorito existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado"));
        validarAccesoALista(existente.getListaId());
        repository.deleteById(id);
    }

    private void validarAccesoALista(Long listaId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada"));

        AuthenticatedUser user = obtenerUsuarioAutenticado();
        if (user != null && !user.isAdmin() && (lista.getUsuarioId() == null || !lista.getUsuarioId().equals(user.id()))) {
            throw new AccesoDenegadoException("No tenés permiso para operar sobre una lista ajena");
        }
    }

    private AuthenticatedUser obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    private FavoritoResponseDTO mapearAResponse(Favorito favorito) {
        return new FavoritoResponseDTO(
            favorito.getId(),
            favorito.getProductoId(),
            favorito.getListaId(),
            favorito.getNotaPersonal(),
            favorito.getFechaAgregado()
        );
    }
}