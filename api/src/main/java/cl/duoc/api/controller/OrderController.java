package cl.duoc.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // Lista en memoria concurrente para almacenar los pedidos
    private final List<Map<String, Object>> ordersList = new CopyOnWriteArrayList<>();

    // 1. Obtener todos los pedidos guardados
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(ordersList);
    }

    // 2. Recibir y guardar un nuevo pedido
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData) {
        // Asignar ID simulado, fecha y estado por defecto
        orderData.put("id", ordersList.size() + 1);
        orderData.put("fecha", LocalDate.now().toString());
        orderData.put("estado", "COMPLETADO");

        // Guardar el pedido en la lista
        ordersList.add(orderData);

        return ResponseEntity.ok(orderData);
    }
}