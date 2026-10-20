
package cl.duoc.ms_orders.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cl.duoc.ms_orders.service.OrderNotificationService;

import org.springframework.amqp.AmqpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // ==========================================
    // DEPENDENCIAS
    // ==========================================

    private final OrderNotificationService notificationService;

    public OrderController(
            OrderNotificationService notificationService) {

        this.notificationService = notificationService;
    }

    // ==========================================
    // ALMACENAMIENTO TEMPORAL DE PEDIDOS
    // ==========================================

    private final List<Map<String, Object>> ordersList =
            new CopyOnWriteArrayList<>();

    // ==========================================
    // 1. OBTENER TODOS LOS PEDIDOS
    // ==========================================

    @GetMapping
    public ResponseEntity<?> getAllOrders() {

        return ResponseEntity.ok(ordersList);
    }

    // ==========================================
    // 2. CREAR NUEVO PEDIDO
    // ==========================================

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> orderData) {

        // Asignar ID, fecha y estado del pedido
        orderData.put("id", ordersList.size() + 1);
        orderData.put("fecha", LocalDate.now().toString());
        orderData.put("estado", "COMPLETADO");

        // Guardar el pedido
        ordersList.add(orderData);

        System.out.println(
                "[MS-ORDERS] Pedido registrado: "
                + orderData.get("id")
        );

        // ==========================================
        // PUBLICAR NOTIFICACION EN RABBITMQ
        // ==========================================

        try {

            notificationService.notificarPedido(
                    orderData.get("id")
            );

        } catch (AmqpException e) {

            System.err.println(
                    "[MS-ORDERS] Error publicando mensaje: "
                    + e.getMessage()
            );
        }

        return ResponseEntity.ok(orderData);
    }
}
