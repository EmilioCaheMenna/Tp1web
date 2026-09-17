package com.example.demo.service;

import com.example.demo.dto.ProductoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {

    private final RestClient restClient;

    public ProductoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ProductoDTO> obtenerTodos() {
        JsonNode response = restClient.get()
                .uri("https://dummyjson.com/products")
                .retrieve()
                .body(JsonNode.class);

        List<ProductoDTO> productos = new ArrayList<>();
        if (response != null && response.has("products")) {
            for (JsonNode node : response.get("products")) {
                productos.add(new ProductoDTO(
                        node.get("id").asLong(),
                        node.get("title").asText(),
                        node.get("description").asText(),
                        node.get("price").asDouble()
                ));
            }
        }
        return productos;
    }

    public ProductoDTO obtenerPorId(Long id) {
        JsonNode node = restClient.get()
                .uri("https://dummyjson.com/products/{id}", id)
                .retrieve()
                .body(JsonNode.class);

        if (node != null && node.has("id")) {
            return new ProductoDTO(
                    node.get("id").asLong(),
                    node.get("title").asText(),
                    node.get("description").asText(),
                    node.get("price").asDouble()
            );
        }
        return null;
    }
}