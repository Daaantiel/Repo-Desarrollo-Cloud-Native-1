
package com.example.rabbitmq_tutorials;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterLogger {

    // ==========================================
    // REGISTRO DE ERRORES EMAIL
    // ==========================================

    @RabbitListener(queues = RabbitMQConfig.DLQ_EMAIL)
    public void logEmailError(String message) {

        System.out.println(
            "[DLQ - EMAIL] Mensaje fallido: " + message
        );
    }


    // ==========================================
    // REGISTRO DE ERRORES STOCK
    // ==========================================

    @RabbitListener(queues = RabbitMQConfig.DLQ_STOCK)
    public void logStockError(String message) {

        System.out.println(
            "[DLQ - STOCK] Mensaje fallido: " + message
        );
    }


    // ==========================================
    // REGISTRO DE ERRORES FACTURA
    // ==========================================

    @RabbitListener(queues = RabbitMQConfig.DLQ_INVOICE)
    public void logInvoiceError(String message) {

        System.out.println(
            "[DLQ - FACTURA] Mensaje fallido: " + message
        );
    }
}
