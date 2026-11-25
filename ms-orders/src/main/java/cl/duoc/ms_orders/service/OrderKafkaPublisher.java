package cl.duoc.ms_orders.service;

import cl.duoc.ms_orders.kafka.OrderPaidEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** Publicacion Kafka independiente de RabbitMQ y posterior al commit en PostgreSQL. */
@Service
public class OrderKafkaPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderKafkaPublisher.class);
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;

    public OrderKafkaPublisher(KafkaTemplate<String, String> kafka, JsonMapper json) {
        this.kafka = kafka;
        this.json = json;
    }

    public void publicarPedidoPagado(Long orderId, String userId) {
        publicarCambioEstado(orderId, "OrderPaid", "PAGADO", userId);
    }

    // Mantiene exactamente el mismo contrato JSON que consumen report y audit.
    public boolean publicarCambioEstado(Long orderId, String tipoEvento,
                                     String estado, String userId) {
        var evento = new OrderPaidEvent(
                UUID.randomUUID().toString(), Instant.now(), orderId,
                tipoEvento, estado, userId);
        try {
            var metadata = kafka.send("orders.events", String.valueOf(orderId),
                    json.writeValueAsString(evento))
                    .get(8, TimeUnit.SECONDS).getRecordMetadata();
            log.info("[MS-ORDERS][KAFKA] Pedido #{} {} enviado: particion={} offset={}",
                    orderId, tipoEvento, metadata.partition(), metadata.offset());
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("[MS-ORDERS][KAFKA] Publicacion interrumpida para pedido #{}", orderId, ex);
            return false;
        } catch (Exception ex) {
            // PostgreSQL ya confirmo el cambio; la falla no revierte el estado.
            log.error("[MS-ORDERS][KAFKA] Fallo al publicar pedido #{} evento {}",
                    orderId, tipoEvento, ex);
            return false;
        }
    }
}
