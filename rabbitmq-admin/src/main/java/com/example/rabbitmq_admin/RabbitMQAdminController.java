
package com.example.rabbitmq_admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class RabbitMQAdminController {

    // ==========================================
    // 1. INYECCION DEL SERVICIO DE ADMINISTRACION
    // ==========================================

    private final RabbitAdminService adminService;

    public RabbitMQAdminController(
            RabbitAdminService adminService) {

        this.adminService = adminService;
    }

    // ==========================================
    // 2. CREAR COLA
    // ==========================================

    @PostMapping("/queues/{name}")
    public ResponseEntity<String> createQueue(
            @PathVariable String name) {

        validarNombre(name);

        adminService.crearCola(name);

        return ResponseEntity.ok(
            "Cola creada: " + name
        );
    }

    // ==========================================
    // 3. ELIMINAR COLA
    // ==========================================

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> deleteQueue(
            @PathVariable String name) {

        validarNombre(name);

        boolean deleted =
                adminService.eliminarCola(name);

        if (!deleted) {
            return ResponseEntity.status(
                HttpStatus.NOT_FOUND
            ).body(
                "Cola no encontrada: " + name
            );
        }

        return ResponseEntity.ok(
            "Cola eliminada: " + name
        );
    }

    // ==========================================
    // 4. CREAR EXCHANGE
    // ==========================================

    @PostMapping("/exchanges/{name}")
    public ResponseEntity<String> createExchange(
            @PathVariable String name,
            @RequestParam(defaultValue = "direct") String type) {

        validarNombre(name);
        validarTipoExchange(type);

        adminService.crearExchange(name, type);

        return ResponseEntity.ok(
            "Exchange creado: " + name
            + " | Tipo: " + type
        );
    }

    // ==========================================
    // 5. ELIMINAR EXCHANGE
    // ==========================================

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<String> deleteExchange(
            @PathVariable String name) {

        validarNombre(name);

        boolean deleted =
                adminService.eliminarExchange(name);

        if (!deleted) {
            return ResponseEntity.status(
                HttpStatus.NOT_FOUND
            ).body(
                "Exchange no encontrado: " + name
            );
        }

        return ResponseEntity.ok(
            "Exchange eliminado: " + name
        );
    }

    // ==========================================
    // 6. CREAR BINDING
    // ==========================================

    @PostMapping("/bindings")
    public ResponseEntity<String> createBinding(
            @RequestParam String queue,
            @RequestParam String exchange,
            @RequestParam String key) {

        validarNombre(queue);
        validarNombre(exchange);
        validarRoutingKey(key);

        adminService.crearBinding(
            queue,
            exchange,
            key
        );

        return ResponseEntity.ok(
            "Binding creado: " + exchange
            + " -> " + queue
            + " | Routing key: " + key
        );
    }

    // ==========================================
    // 7. ELIMINAR BINDING
    // ==========================================

    @DeleteMapping("/bindings")
    public ResponseEntity<String> deleteBinding(
            @RequestParam String queue,
            @RequestParam String exchange,
            @RequestParam String key) {

        validarNombre(queue);
        validarNombre(exchange);
        validarRoutingKey(key);

        adminService.eliminarBinding(
            queue,
            exchange,
            key
        );

        return ResponseEntity.ok(
            "Solicitud de eliminacion del binding: "
            + exchange + " -> " + queue
        );
    }

    // ==========================================
    // 8. VALIDACION DE NOMBRES
    // ==========================================

    private void validarNombre(String nombre) {

        // Evita nombres vacios y caracteres no permitidos
        if (nombre == null ||
            !nombre.matches("[a-zA-Z0-9][a-zA-Z0-9._-]{0,99}")) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nombre invalido. Use entre 1 y 100 "
                + "caracteres: letras, numeros, punto, "
                + "guion o guion bajo."
            );
        }

        // Evita utilizar nombres reservados de RabbitMQ
        if (nombre.toLowerCase().startsWith("amq.")) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No se permiten nombres reservados amq."
            );
        }
    }

    // ==========================================
    // 9. VALIDACION DEL TIPO DE EXCHANGE
    // ==========================================

    private void validarTipoExchange(String tipo) {

        if (tipo == null ||
            (!tipo.equalsIgnoreCase("direct") &&
             !tipo.equalsIgnoreCase("topic"))) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tipo invalido. Use direct o topic."
            );
        }
    }

    // ==========================================
    // 10. VALIDACION DE ROUTING KEY
    // ==========================================

    private void validarRoutingKey(String key) {

        if (key == null ||
            !key.matches("[a-zA-Z0-9._*#-]{1,100}")) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Routing key invalida."
            );
        }
    }
}
