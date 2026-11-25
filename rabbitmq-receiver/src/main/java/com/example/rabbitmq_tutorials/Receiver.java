
package com.example.rabbitmq_tutorials;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class Receiver {

    // ==========================================
    // CONFIGURACION DE REINTENTOS
    // ==========================================

    // Tres intentos en total:
    // 1 intento inicial + 2 reintentos.
    private static final int MAX_INTENTOS = 3;

    // ==========================================
    // 1. RECEPCION EMAIL
    // ==========================================

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE_EMAIL,
        ackMode = "MANUAL"
    )
    public void receiveEmail(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        procesarConConfirmacion(
            "EMAIL",
            message,
            channel,
            deliveryTag
        );
    }

    // ==========================================
    // 2. RECEPCION STOCK
    // ==========================================

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE_STOCK,
        ackMode = "MANUAL"
    )
    public void receiveStock(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        procesarConConfirmacion(
            "STOCK",
            message,
            channel,
            deliveryTag
        );
    }

    // ==========================================
    // 3. RECEPCION FACTURA
    // ==========================================

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE_INVOICE,
        ackMode = "MANUAL"
    )
    public void receiveInvoice(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        procesarConConfirmacion(
            "FACTURA",
            message,
            channel,
            deliveryTag
        );
    }

    // ==========================================
    // 4. PROCESAMIENTO CON ACK / NACK
    // ==========================================

    private void procesarConConfirmacion(
            String tipo,
            String message,
            Channel channel,
            long deliveryTag
    ) throws IOException {

        System.out.println(
            "[RECEIVER - " + tipo + "] Mensaje recibido: "
            + message
        );

        // ======================================
        // ERROR DEFINITIVO
        // ======================================

        if (message.trim().equalsIgnoreCase("ERROR")) {

            System.out.println(
                "[" + tipo + "] Error definitivo."
            );

            // NACK sin reencolar.
            // El mensaje se envia a su DLQ.
            channel.basicNack(
                deliveryTag,
                false,
                false
            );

            System.out.println(
                "[" + tipo + "] NACK enviado correctamente."
            );

            return;
        }

        // ======================================
        // REINTENTOS CONTROLADOS
        // ======================================

        for (int intento = 1;
             intento <= MAX_INTENTOS;
             intento++) {

            try {

                System.out.println(
                    "[" + tipo + "] Intento "
                    + intento + "/" + MAX_INTENTOS
                );

                // Procesamiento del mensaje
                procesarMensaje(message, intento);

                // Si no hubo errores, confirmar.
                channel.basicAck(
                    deliveryTag,
                    false
                );

                System.out.println(
                    "[" + tipo + "] Procesado correctamente."
                );

                System.out.println(
                    "[" + tipo + "] ACK enviado correctamente."
                );

                return;

            } catch (RuntimeException e) {

                // Error durante el procesamiento.
                System.err.println(
                    "[" + tipo + "] Fallo en intento "
                    + intento + ": " + e.getMessage()
                );

                if (intento < MAX_INTENTOS) {

                    System.out.println(
                        "[" + tipo + "] Reintentando..."
                    );
                }
            }
        }

        // ======================================
        // SE AGOTARON LOS REINTENTOS
        // ======================================

        System.err.println(
            "[" + tipo + "] Se agotaron los "
            + MAX_INTENTOS + " intentos."
        );

        // Rechazar definitivamente el mensaje.
        // RabbitMQ lo enviara a la DLQ.
        channel.basicNack(
            deliveryTag,
            false,
            false
        );

        System.out.println(
            "[" + tipo + "] NACK enviado a DLQ."
        );
    }

    // ==========================================
    // 5. SIMULACION DE PROCESAMIENTO
    // ==========================================

    private void procesarMensaje(
            String message,
            int intento
    ) {

        String contenido = message.trim();

        // ERROR TEMPORAL PERSISTENTE:
        // Falla en todos los intentos.
        if (contenido.equalsIgnoreCase("ERROR_TEMPORAL")) {

            throw new IllegalStateException(
                "Fallo temporal simulado"
            );
        }

        // ERROR TEMPORAL RECUPERABLE:
        // Falla en el primer intento.
        // Se recupera en el segundo.
        if (contenido.equalsIgnoreCase("ERROR_TEMPORAL_OK")
                && intento == 1) {

            throw new IllegalStateException(
                "Servicio temporalmente no disponible"
            );
        }

        // Mensaje procesado sin errores.
        // En este prototipo no se modifica
        // el stock ni se generan documentos.
    }

    // ==========================================
    // 6. PRUEBA ORIGINAL HELLO
    // ==========================================

    @RabbitListener(queues = RabbitMQConfig.QUEUE_HELLO)
    public void receiveHello(String message) {

        System.out.println(
            "[RECEIVER - HELLO] Mensaje recibido: "
            + message
        );
    }
}
