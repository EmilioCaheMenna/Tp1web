package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ListaRequestDTO(
    @NotBlank(message = "El nombre de la lista es obligatorio")
    String nombre
) {}
