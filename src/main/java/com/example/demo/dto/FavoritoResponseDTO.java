package com.example.demo.dto;

import java.time.LocalDateTime;

public record FavoritoResponseDTO(
    Long id,
    Long productoId,
    Long listaId,
    String notaPersonal,
    LocalDateTime fechaAgregado
) {}
