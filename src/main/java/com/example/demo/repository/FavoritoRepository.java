package com.example.demo.repository;

import com.example.demo.model.Favorito;
import java.util.List;
import java.util.Optional;

public interface FavoritoRepository {
    List<Favorito> findAll();
    List<Favorito> findByListaId(Long listaId);
    Optional<Favorito> findById(Long id);
    Favorito save(Favorito favorito);
    void deleteById(Long id);
    boolean existsById(Long id);
}