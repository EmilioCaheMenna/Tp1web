package com.example.demo.repository;

import com.example.demo.model.Favorito;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Favorito> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Favorito save(Favorito favorito) {
        FavoritoEntity entity = toEntity(favorito);
        FavoritoEntity saved = jpaRepository.save(entity);
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

    private Favorito toDomain(FavoritoEntity entity) {
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getLista() != null ? entity.getLista().getId() : null,
                entity.getNota(),
                entity.getFechaAlta()
        );
    }

    private FavoritoEntity toEntity(Favorito domain) {
        ListaEntity listaEntity = null;
        if (domain.getListaId() != null) {
            listaEntity = new ListaEntity();
            listaEntity.setId(domain.getListaId());
        }
        return new FavoritoEntity(
                domain.getId(),
                domain.getProductoId(),
                domain.getNotaPersonal(),
                domain.getFechaAgregado(),
                listaEntity
        );
    }
}
