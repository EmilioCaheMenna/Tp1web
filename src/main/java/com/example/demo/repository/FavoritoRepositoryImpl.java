package com.example.demo.repository;

import com.example.demo.model.Favorito;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository // Esta anotación es clave para que Spring lo detecte
public class FavoritoRepositoryImpl implements FavoritoRepository {

    // Nuestra "base de datos" en memoria
    private final Map<Long, Favorito> baseDeDatos = new ConcurrentHashMap<>();
    // Generador de IDs autoincrementales
    private final AtomicLong generadorId = new AtomicLong(1);

    @Override
    public List<Favorito> findAll() {
        return new ArrayList<>(baseDeDatos.values());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return Optional.ofNullable(baseDeDatos.get(id));
    }

    @Override
    public Favorito save(Favorito favorito) {
        // Si no tiene ID, es un favorito nuevo, le asignamos uno
        if (favorito.getId() == null) {
            favorito.setId(generadorId.getAndIncrement());
        }
        // Guardamos o actualizamos en el Map
        baseDeDatos.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public void deleteById(Long id) {
        baseDeDatos.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return baseDeDatos.containsKey(id);
    }
}