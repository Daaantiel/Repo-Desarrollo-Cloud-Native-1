package cl.duoc.ms_orders.controller;

import cl.duoc.ms_orders.service.OrderNotificationService;
import cl.duoc.ms_orders.service.OrderKafkaPublisher;
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
    private final OrderKafkaPublisher kafkaPublisher;

    public OrderController(OrderPersistenceService persistence,
                           OrderNotificationService notifications,
                           OrderKafkaPublisher kafkaPublisher) {
        this.persistence = persistence;
        this.notifications = notifications;
        this.kafkaPublisher = kafkaPublisher;
    }

    // Endpoints originales del cliente: se mantienen sin cambios funcionales.
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
        // La transacción termina antes de publicar en RabbitMQ y Kafka.
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
        if (resultado.publicar()) {
            kafkaPublisher.publicarPedidoPagado(id, userId);
        }
        return ResponseEntity.ok(respuesta);
    }

    // Exclusivos de administradores. El BFF valida el JWT/rol y envia el rol
    // obtenido del token, nunca del navegador. El token interno protege este MS.
    @GetMapping("/admin")
    public ResponseEntity<?> listarTodosAdmin(
            @RequestHeader(value = "X-User-Role", required = false) String rol) {
        exigirAdmin(rol);
        return ResponseEntity.ok(persistence.listarTodosAdmin());
    }

    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<?> actualizarEstadoAdmin(
            @RequestHeader("X-User-Id") String actorId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        exigirAdmin(rol);
        Object valor = body.getOrDefault("estado", body.get("status"));
        if (!(valor instanceof String estado) || estado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indique estado (o status) como texto");
        }
        // Al regresar de este metodo, la transaccion de PostgreSQL ya termino.
        var resultado = persistence.cambiarEstadoAdmin(id, estado);
        boolean confirmadoKafka = !resultado.publicar();
        if (resultado.publicar()) {
            confirmadoKafka = kafkaPublisher.publicarCambioEstado(
                    id, resultado.evento(), resultado.estado(), actorId);
        }
        Map<String, Object> respuesta = new LinkedHashMap<>(resultado.respuesta());
        respuesta.put("actualizadoPor", actorId);
        respuesta.put("eventoKafka", resultado.publicar() ? resultado.evento() : "SIN_CAMBIOS");
        respuesta.put("publicacionKafka", !resultado.publicar() ? "SIN_CAMBIOS"
                : (confirmadoKafka ? "PUBLICADA" : "ERROR_PUBLICACION"));
        return ResponseEntity.ok(respuesta);
    }

    private void exigirAdmin(String rol) {
        // El rol es una cabecera interna generada SOLO por el BFF a partir de
        // los claims validados. Nunca se debe aceptar del cliente web.
        if (rol == null || !(rol.equalsIgnoreCase("Admin")
                || rol.equalsIgnoreCase("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se requiere rol Admin");
        }
    }
}
