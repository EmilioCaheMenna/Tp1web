package com.example.demo.repository;

import com.example.demo.model.Lista;
import java.util.List;
import java.util.Optional;

public interface ListaRepository {
    List<Lista> findAll();
    Optional<Lista> findById(Long id);
    Lista save(Lista lista);
    void deleteById(Long id);
    boolean existsById(Long id);
}
