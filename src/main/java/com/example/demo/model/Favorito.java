package com.example.demo.model;

import java.time.LocalDateTime;

public class Favorito {
    private Long id;
    private Long productoId;
    private Long listaId;
    private String notaPersonal;
    private LocalDateTime fechaAgregado;

    // Constructor vacío
    public Favorito() {
    }

    // Constructor completo
    public Favorito(Long id, Long productoId, Long listaId, String notaPersonal, LocalDateTime fechaAgregado) {
        this.id = id;
        this.productoId = productoId;
        this.listaId = listaId;
        this.notaPersonal = notaPersonal;
        this.fechaAgregado = fechaAgregado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Long getListaId() { return listaId; }
    public void setListaId(Long listaId) { this.listaId = listaId; }

    public String getNotaPersonal() { return notaPersonal; }
    public void setNotaPersonal(String notaPersonal) { this.notaPersonal = notaPersonal; }

    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}