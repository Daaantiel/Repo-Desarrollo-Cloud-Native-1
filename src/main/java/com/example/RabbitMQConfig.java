package com.example;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {
/**
* Cola "hello".
* Idempotente: si no existe la crea; si existe, la reutiliza.
* durable=false → se borra si RabbitMQ se reinicia (en producción: true).
*/
@Bean
public Queue helloQueue() {
return new Queue("hello", false);
}
}


