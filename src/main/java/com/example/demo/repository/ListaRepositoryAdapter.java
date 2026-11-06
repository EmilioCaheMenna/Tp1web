package com.example.demo.repository;

import com.example.demo.model.Lista;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Lista> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity saved = jpaRepository.save(toEntity(lista));
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    private Lista toDomain(ListaEntity entity) {
        return new Lista(
                entity.getId(),
                entity.getNombre(),
                entity.getUsuario() != null ? entity.getUsuario().getId() : null
        );
    }

    private ListaEntity toEntity(Lista domain) {
        UsuarioEntity usuarioEntity = null;
        if (domain.getUsuarioId() != null) {
            usuarioEntity = new UsuarioEntity();
            usuarioEntity.setId(domain.getUsuarioId());
        }
        return new ListaEntity(domain.getId(), domain.getNombre(), usuarioEntity);
    }
}
