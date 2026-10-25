package cl.duoc.ms_orders.controller;

import cl.duoc.ms_orders.service.OrderNotificationService;
import cl.duoc.ms_orders.service.OrderPersistenceService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderPersistenceService persistence;
    private final OrderNotificationService notifications;

    public OrderController(OrderPersistenceService persistence,
                           OrderNotificationService notifications) {
        this.persistence = persistence;
        this.notifications = notifications;
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(persistence.listar(userId));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(persistence.resumen(userId));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> pedido = persistence.registrar(userId, body);
        System.out.println("[MS-ORDERS] Pedido pendiente: #" + pedido.get("id"));
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PostMapping("/{id}/simulate-payment")
    public ResponseEntity<?> simulatePayment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        if (!(body.get("approved") instanceof Boolean aprobado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "approved debe ser true o false");
        }
        // La transacción termina antes de publicar en RabbitMQ.
        var resultado = persistence.simularPago(id, userId, aprobado);
        Map<String, Object> respuesta = new LinkedHashMap<>(resultado.respuesta());
        if (resultado.publicar()) {
            try {
                notifications.notificarPedido(id);
                respuesta.put("mensajeria", "PUBLICADA");
            } catch (AmqpException e) {
                respuesta.put("mensajeria", "ERROR_PUBLICACION");
                System.err.println("[MS-ORDERS] Fallo RabbitMQ pedido #" + id + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(respuesta);
    }
}
