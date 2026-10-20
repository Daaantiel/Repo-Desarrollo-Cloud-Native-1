
package cl.duoc.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // ==========================================
    // CONEXION CON EL MICROSERVICIO ORDERS
    // ==========================================

    private final RestClient restClient;

    public OrderController(
            @Value("${microservices.orders.base-url}")
            String ordersBaseUrl) {

        this.restClient = RestClient.create(ordersBaseUrl);
    }

    // ==========================================
    // 1. OBTENER PEDIDOS DESDE MS-ORDERS
    // ==========================================

    @GetMapping
    public ResponseEntity<?> getAllOrders() {

        try {

            Object orders = restClient.get()
                    .uri("/api/orders")
                    .retrieve()
                    .body(Object.class);

            System.out.println(
                "[BFF] Consultando pedidos en ms-orders"
            );

            return ResponseEntity.ok(orders);

        } catch (RestClientException e) {

            System.err.println(
                "[BFF] Error consultando ms-orders: "
                + e.getMessage()
            );

            return ResponseEntity.status(
                HttpStatus.BAD_GATEWAY
            ).body("No se pudo consultar ms-orders");
        }
    }

    // ==========================================
    // 2. DELEGAR CREACION DE PEDIDOS
    // ==========================================

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> orderData) {

        try {

            // El BFF recibe el pedido autenticado
            // y lo envia al microservicio Orders.

            Object createdOrder = restClient.post()
                    .uri("/api/orders")
                    .body(orderData)
                    .retrieve()
                    .body(Object.class);

            System.out.println(
                "[BFF] Pedido enviado a ms-orders"
            );

            // ms-orders se encarga de guardar
            // y publicar la notificacion RabbitMQ.

            return ResponseEntity.ok(createdOrder);

        } catch (RestClientException e) {

            System.err.println(
                "[BFF] Error creando pedido: "
                + e.getMessage()
            );

            return ResponseEntity.status(
                HttpStatus.BAD_GATEWAY
            ).body("No se pudo registrar el pedido");
        }
    }
}
