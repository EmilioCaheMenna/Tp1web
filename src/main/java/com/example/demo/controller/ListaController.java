package com.example.demo.controller;

import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.dto.ListaRequestDTO;
import com.example.demo.dto.ListaResponseDTO;
import com.example.demo.service.ListaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Endpoints para la gestión de listas de favoritos")
public class ListaController {

    private final ListaService service;

    public ListaController(ListaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear lista", description = "Crea una nueva lista de favoritos")
    public ResponseEntity<ListaResponseDTO> crearLista(@Valid @RequestBody ListaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    @Operation(summary = "Listar listas", description = "Devuelve todas las listas de favoritos")
    public ResponseEntity<List<ListaResponseDTO>> listarListas() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista", description = "Devuelve los detalles de una lista específica")
    public ResponseEntity<ListaResponseDTO> obtenerLista(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Favoritos de una lista", description = "Devuelve los favoritos que pertenecen a una lista")
    public ResponseEntity<List<FavoritoResponseDTO>> favoritosDeLista(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarFavoritosDeLista(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una lista vacía", description = "Elimina una lista si no tiene favoritos")
    public ResponseEntity<Void> eliminarLista(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @Operation(summary = "Mover favoritos de una lista a otra", description = "Reasigna todos los favoritos de la lista origen a la lista destino, y luego elimina la lista origen.")
    public ResponseEntity<Void> moverFavoritos(
            @PathVariable Long origenId,
            @RequestParam Long destinoId
    ) {
        service.moverFavoritos(origenId, destinoId);
        return ResponseEntity.noContent().build();
    }
}
