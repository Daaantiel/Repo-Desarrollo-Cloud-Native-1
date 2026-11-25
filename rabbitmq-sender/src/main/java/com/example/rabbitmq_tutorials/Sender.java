
package com.example.rabbitmq_tutorials;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private final RabbitTemplate rabbitTemplate;

    public Sender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ==========================================
    // ENVIO MEDIANTE EXCHANGE
    // ==========================================

    public void sendMessage(String exchange,
                            String routingKey,
                            String message) {

        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            message
        );

        System.out.println(
            "[SENDER] Exchange: " + exchange
            + " | Routing key: " + routingKey
            + " | Mensaje: " + message
        );
    }
}
