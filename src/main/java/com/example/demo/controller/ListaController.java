package com.example.demo.controller;

import com.example.demo.dto.ListaRequestDTO;
import com.example.demo.dto.ListaResponseDTO;
import com.example.demo.service.ListaService;
import com.example.demo.repository.FavoritoEntity;
import com.example.demo.repository.FavoritoJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Endpoints para la gestin de listas de favoritos")
public class ListaController {

    private final ListaService service;
    private final FavoritoJpaRepository favoritoJpaRepository; // Directamente por simplicidad

    public ListaController(ListaService service, FavoritoJpaRepository favoritoJpaRepository) {
        this.service = service;
        this.favoritoJpaRepository = favoritoJpaRepository;
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
    @Operation(summary = "Obtener una lista", description = "Devuelve los detalles de una lista especfica")
    public ResponseEntity<ListaResponseDTO> obtenerLista(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Favoritos de una lista", description = "Devuelve los favoritos que pertenecen a una lista")
    public ResponseEntity<List<FavoritoEntity>> favoritosDeLista(@PathVariable Long id) {
        // En una app real, mapeariamos esto a un DTO de respuesta
        return ResponseEntity.ok(favoritoJpaRepository.findByListaId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una lista vaca", description = "Elimina una lista si no tiene favoritos")
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
