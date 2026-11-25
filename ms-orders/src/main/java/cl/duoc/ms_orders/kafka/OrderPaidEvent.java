package cl.duoc.ms_orders.kafka;

import java.time.Instant;

/** Contrato compatible con los consumidores ms-kafka-report y ms-kafka-audit. */
public record OrderPaidEvent(
        String eventId,
        Instant occurredAt,
        Long orderId,
        String eventType,
        String status,
        String userId) {}
