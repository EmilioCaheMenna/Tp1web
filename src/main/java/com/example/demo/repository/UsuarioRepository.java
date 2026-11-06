package com.example.demo.repository;

import com.example.demo.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByEmail(String email);
    Usuario save(Usuario usuario);
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByEmail(String email);
}
