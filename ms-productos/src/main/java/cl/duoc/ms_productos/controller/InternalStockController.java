package cl.duoc.ms_productos.controller;

import cl.duoc.ms_productos.service.StockReservationService;
import cl.duoc.ms_productos.service.StockReservationService.Item;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/stock")
public class InternalStockController {
    private final StockReservationService service;
    private final String sharedToken;

    public InternalStockController(StockReservationService service,
            @Value("${ORDERS_INTERNAL_TOKEN}") String sharedToken) {
        this.service = service;
        this.sharedToken = sharedToken;
    }

    public record Solicitud(List<Item> items) {}

    @PostMapping("/reserve")
    public Map<String, Object> reservar(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody Solicitud solicitud) {
        boolean autorizado = token != null && sharedToken != null && !sharedToken.isBlank()
            && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                sharedToken.getBytes(StandardCharsets.UTF_8));
        if (!autorizado) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        service.reservar(solicitud.items());
        return Map.of("resultado", "RESERVADO");
    }
}
