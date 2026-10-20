
package cl.duoc.ms_orders.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {

    // ==========================================
    // 1. CONFIGURACION DE MENSAJERIA
    // ==========================================

    // Valores obtenidos desde application.yml
    private final String exchange;
    private final String keyEmail;
    private final String keyStock;
    private final String keyInvoice;

    private final RabbitTemplate rabbitTemplate;

    // ==========================================
    // 2. INYECCION DE DEPENDENCIAS
    // ==========================================

    public OrderNotificationService(
            RabbitTemplate rabbitTemplate,
            @Value("${messaging.exchange}") String exchange,
            @Value("${messaging.routing.email}") String keyEmail,
            @Value("${messaging.routing.stock}") String keyStock,
            @Value("${messaging.routing.invoice}") String keyInvoice) {

        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.keyEmail = keyEmail;
        this.keyStock = keyStock;
        this.keyInvoice = keyInvoice;
    }

    // ==========================================
    // 3. PUBLICAR MENSAJES DEL PEDIDO
    // ==========================================

    public void notificarPedido(Object pedidoId) {

        // Convertir el identificador del pedido a texto
        String id = String.valueOf(pedidoId);

        // ======================================
        // EMAIL: NOTIFICACION DE COMPRA
        // ======================================

        String mensajeEmail =
                "Nuevo pedido #" + id
                + " registrado en PokeMarket360";

        publicar(keyEmail, mensajeEmail);

        // ======================================
        // STOCK: SOLICITUD DE INVENTARIO
        // ======================================

        String mensajeStock =
                "Reservar stock para el pedido #" + id
                + " de PokeMarket360";

        publicar(keyStock, mensajeStock);

        // ======================================
        // FACTURA: SOLICITUD DE COMPROBANTE
        // ======================================

        String mensajeFactura =
                "Generar comprobante del pedido #" + id
                + " de PokeMarket360";

        publicar(keyInvoice, mensajeFactura);
    }

    // ==========================================
    // 4. METODO REUTILIZABLE DE PUBLICACION
    // ==========================================

    private void publicar(String routingKey, String mensaje) {

        // Publicar mensaje en el exchange configurado
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                mensaje
        );

        // Registrar la solicitud de publicacion
        System.out.println(
                "[MS-ORDERS] Mensaje publicado: "
                + routingKey + " -> " + mensaje
        );
    }
}
