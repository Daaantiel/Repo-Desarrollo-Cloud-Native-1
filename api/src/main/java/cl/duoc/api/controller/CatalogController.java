package cl.duoc.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/catalog/products")
public class CatalogController {

    private final RestClient restClient;

    public CatalogController(@Value("${microservices.productos.base-url}") String productosUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(productosUrl)
                .build();
    }

    @GetMapping
    public List<?> listarProductos() {
        return restClient.get()
                .uri("/api/catalog/products")
                .retrieve()
                .body(List.class);
    }
}