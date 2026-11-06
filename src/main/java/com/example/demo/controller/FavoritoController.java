package com.example.demo.controller;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Endpoints para la gestión de favoritos (Requiere autenticación con token JWT)")
@SecurityRequirement(name = "bearerAuth")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @Operation(summary = "Crear un nuevo favorito", description = "Crea un favorito dentro de una lista propia (Requiere autenticación JWT)")
    @PostMapping
    public ResponseEntity<FavoritoResponseDTO> crear(@Valid @RequestBody FavoritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoService.crear(dto));
    }

    @Operation(summary = "Listar todos los favoritos", description = "Lista los favoritos de las listas del usuario autenticado, o todos si es ADMIN (Requiere autenticación JWT)")
    @GetMapping
    public ResponseEntity<List<FavoritoResponseDTO>> listar() {
        return ResponseEntity.ok(favoritoService.listarTodos());
    }

    @Operation(summary = "Obtener un favorito por su ID", description = "Obtiene el detalle de un favorito propio (Requiere autenticación JWT)")
    @GetMapping("/{id}")
    public ResponseEntity<FavoritoResponseDTO> obtenerUno(@PathVariable Long id) {
        return ResponseEntity.ok(favoritoService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar un favorito existente", description = "Actualiza un favorito propio (Requiere autenticación JWT)")
    @PutMapping("/{id}")
    public ResponseEntity<FavoritoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody FavoritoRequestDTO dto) {
        return ResponseEntity.ok(favoritoService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar un favorito", description = "Elimina un favorito propio (Requiere autenticación JWT)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        favoritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}