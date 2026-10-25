package cl.duoc.ms_orders.service;

import cl.duoc.ms_orders.model.OrderItemEntity;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryClient {
    private final RestClient client;
    private final String internalToken;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl,
            @Value("${ORDERS_INTERNAL_TOKEN}") String token) {
        this.client = RestClient.create(baseUrl);
        this.internalToken = token;
    }

    public record StockItem(Long productId, Integer cantidad) {}

    public void reservar(List<OrderItemEntity> items) {
        List<StockItem> lineas = items.stream()
            .map(i -> new StockItem(i.getProductId(), i.getQuantity()))
            .toList();
        try {
            client.post()
                .uri("/internal/stock/reserve")
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("items", lineas))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente. Actualiza el catalogo y revisa cantidades.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "No se pudo validar el inventario");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Servicio de inventario no disponible");
        }
    }
}
