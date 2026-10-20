package com.example.rabbitmq_tutorials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitmqTutorialsApplication {

    public static void main(String[] args) {

        // Inicia el microservicio Receiver.
        SpringApplication.run(
            RabbitmqTutorialsApplication.class,
            args
        );

        System.out.println(
            "[RECEIVER] Esperando mensajes de RabbitMQ..."
        );
    }
}