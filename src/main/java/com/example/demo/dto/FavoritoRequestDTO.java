package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoritoRequestDTO(
    @NotNull(message = "El ID del producto es obligatorio")
    Long productoId,

    @NotNull(message = "El ID de la lista es obligatorio")
    Long listaId,

    @NotBlank(message = "La nota personal no puede estar vacia")
    String notaPersonal
) {}
