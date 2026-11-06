package com.example.demo.service;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.exception.EmailYaRegistradoException;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioResponseDTO registrar(AuthRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new EmailYaRegistradoException("El email ya está registrado");
        }

        Usuario nuevo = new Usuario();
        nuevo.setEmail(dto.email());
        nuevo.setPasswordHash(passwordEncoder.encode(dto.password()));
        nuevo.setRol("USER");

        Usuario guardado = usuarioRepository.save(nuevo);
        return new UsuarioResponseDTO(guardado.getId(), guardado.getEmail(), guardado.getRol());
    }

    public AuthResponseDTO login(AuthRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
        return new AuthResponseDTO(token);
    }
}
