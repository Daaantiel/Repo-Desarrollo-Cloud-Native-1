
package com.example.rabbitmq_admin;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    // ==========================================
    // DEPENDENCIA DE ADMINISTRACION RABBITMQ
    // ==========================================

    private final AmqpAdmin rabbitAdmin;

    public RabbitAdminService(AmqpAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // ==========================================
    // 1. CREAR COLA
    // ==========================================

    public void crearCola(String nombre) {

        Queue cola = new Queue(nombre, true);

        rabbitAdmin.declareQueue(cola);

        System.out.println(
            "[ADMIN] Cola creada: " + nombre
        );
    }

    // ==========================================
    // 2. ELIMINAR COLA
    // ==========================================

    public boolean eliminarCola(String nombre) {

        boolean eliminada =
                rabbitAdmin.deleteQueue(nombre);

        System.out.println(
            "[ADMIN] Solicitud de eliminar cola: " + nombre
        );

        return eliminada;
    }

    // ==========================================
    // 3. CREAR EXCHANGE
    // ==========================================

    public void crearExchange(
            String nombre,
            String tipo) {

        if (tipo.equalsIgnoreCase("direct")) {

            rabbitAdmin.declareExchange(
                new DirectExchange(nombre, true, false)
            );

        } else if (tipo.equalsIgnoreCase("topic")) {

            rabbitAdmin.declareExchange(
                new TopicExchange(nombre, true, false)
            );

        } else {

            throw new IllegalArgumentException(
                "Tipo de exchange no valido"
            );
        }

        System.out.println(
            "[ADMIN] Exchange creado: " + nombre
        );
    }

    // ==========================================
    // 4. ELIMINAR EXCHANGE
    // ==========================================

    public boolean eliminarExchange(String nombre) {

        return rabbitAdmin.deleteExchange(nombre);
    }

    // ==========================================
    // 5. CREAR BINDING
    // ==========================================

    public void crearBinding(
            String cola,
            String exchange,
            String routingKey) {

        Binding binding = new Binding(
            cola,
            Binding.DestinationType.QUEUE,
            exchange,
            routingKey,
            null
        );

        rabbitAdmin.declareBinding(binding);

        System.out.println(
            "[ADMIN] Binding creado: "
            + exchange + " -> " + cola
        );
    }

    // ==========================================
    // 6. ELIMINAR BINDING
    // ==========================================

    public void eliminarBinding(
            String cola,
            String exchange,
            String routingKey) {

        Binding binding = new Binding(
            cola,
            Binding.DestinationType.QUEUE,
            exchange,
            routingKey,
            null
        );

        rabbitAdmin.removeBinding(binding);

        System.out.println(
            "[ADMIN] Solicitud de eliminar binding: "
            + exchange + " -> " + cola
        );
    }
}
