package cl.duoc.api.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final RestClient client;
    private final String internalToken;

    public OrderController(
            @Value("${microservices.orders.base-url}") String baseUrl,
            @Value("${ORDERS_INTERNAL_TOKEN}") String internalToken) {
        this.client = RestClient.create(baseUrl);
        this.internalToken = internalToken;
    }

    // Identidad sólo desde JWT VALIDADO; nunca del body o del navegador.
    private String identity(Jwt jwt) {
        String tid = jwt.getClaimAsString("tid");
        String oid = jwt.getClaimAsString("oid");
        if (tid == null || tid.isBlank() || oid == null || oid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token sin identidad Microsoft Entra esperada");
        }
        return tid + ":" + oid;
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        try {
            Object result = client.get()
                    .uri("/api/orders")
                    .header("X-Internal-Token", internalToken)
                    .header("X-User-Id", identity(jwt))
                    .retrieve().body(Object.class);
            return ResponseEntity.ok(result);
        } catch (RestClientResponseException e) {
            return upstreamError(e);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error consultando ms-orders");
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@AuthenticationPrincipal Jwt jwt) {
        try {
            Object result = client.get()
                    .uri("/api/orders/summary")
                    .header("X-Internal-Token", internalToken)
                    .header("X-User-Id", identity(jwt))
                    .retrieve().body(Object.class);
            return ResponseEntity.ok(result);
        } catch (RestClientResponseException e) {
            return upstreamError(e);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error consultando resumen");
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Object> body) {
        try {
            Object result = client.post()
                    .uri("/api/orders")
                    .header("X-Internal-Token", internalToken)
                    .header("X-User-Id", identity(jwt))
                    .body(body).retrieve().body(Object.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RestClientResponseException e) {
            return upstreamError(e);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error al crear pedido");
        }
    }

    @PostMapping("/{id}/simulate-payment")
    public ResponseEntity<?> simulatePayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Object result = client.post()
                    .uri("/api/orders/{id}/simulate-payment", id)
                    .header("X-Internal-Token", internalToken)
                    .header("X-User-Id", identity(jwt))
                    .body(body).retrieve().body(Object.class);
            return ResponseEntity.ok(result);
        } catch (RestClientResponseException e) {
            return upstreamError(e);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error al simular pago");
        }
    }

    private ResponseEntity<?> upstreamError(RestClientResponseException e) {
        // Conserva el estado 400/401/404/409 del microservicio.
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("error", "Solicitud rechazada por ms-orders", "httpStatus", e.getStatusCode().value()));
    }
}
