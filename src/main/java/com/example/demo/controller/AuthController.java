package com.example.demo.controller;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.service.UsuarioServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para registro e inicio de sesión con emisión de JWT")
public class AuthController {

    private final UsuarioServiceImpl usuarioService;

    public AuthController(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un nuevo usuario", description = "Registra un usuario con rol USER y contraseña hasheada con BCrypt. Devuelve 409 Conflict si el email ya existe.")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody AuthRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT", description = "Valida las credenciales contra el hash almacenado y emite un token JWT firmado con el id y rol del usuario. Devuelve 401 Unauthorized si las credenciales son inválidas.")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.login(dto));
    }
}
