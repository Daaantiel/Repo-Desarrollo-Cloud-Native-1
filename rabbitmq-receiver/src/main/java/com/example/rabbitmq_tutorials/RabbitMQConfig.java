
package com.example.rabbitmq_tutorials;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ==========================================
    // 1. NOMBRES DE COLAS
    // ==========================================

    public static final String QUEUE_HELLO = "hello";
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_STOCK = "q.cmd.stock";
    public static final String QUEUE_INVOICE = "q.cmd.invoice";

    // ==========================================
    // 2. NOMBRES DE DLQ
    // ==========================================

    public static final String DLQ_EMAIL = "q.cmd.email.dlq";
    public static final String DLQ_STOCK = "q.cmd.stock.dlq";
    public static final String DLQ_INVOICE = "q.cmd.invoice.dlq";

    // ==========================================
    // 3. NOMBRES DE EXCHANGES
    // ==========================================

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // ==========================================
    // 4. ROUTING KEYS
    // ==========================================

    public static final String KEY_EMAIL = "email.send";
    public static final String KEY_STOCK = "stock.reserve";
    public static final String KEY_INVOICE = "invoice.gen";

    // ==========================================
    // 5. DECLARACION DE COLAS
    // ==========================================

    @Bean
    public Queue helloQueue() {
        return QueueBuilder.durable(QUEUE_HELLO).build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_EMAIL)
                .build();
    }

    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(QUEUE_STOCK)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_STOCK)
                .build();
    }

    @Bean
    public Queue invoiceQueue() {
        return QueueBuilder.durable(QUEUE_INVOICE)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(KEY_INVOICE)
                .build();
    }

    // ==========================================
    // 6. DECLARACION DE DEAD LETTER QUEUES
    // ==========================================

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(DLQ_EMAIL).build();
    }

    @Bean
    public Queue stockDlq() {
        return QueueBuilder.durable(DLQ_STOCK).build();
    }

    @Bean
    public Queue invoiceDlq() {
        return QueueBuilder.durable(DLQ_INVOICE).build();
    }

    // ==========================================
    // 7. DECLARACION DE EXCHANGES
    // ==========================================

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // ==========================================
    // 8. BINDINGS DIRECT
    // ==========================================

    @Bean
    public Binding directEmail(
            @Qualifier("emailQueue") Queue queue,
            @Qualifier("directExchange") DirectExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY_EMAIL);
    }

    @Bean
    public Binding directStock(
            @Qualifier("stockQueue") Queue queue,
            @Qualifier("directExchange") DirectExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY_STOCK);
    }

    @Bean
    public Binding directInvoice(
            @Qualifier("invoiceQueue") Queue queue,
            @Qualifier("directExchange") DirectExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY_INVOICE);
    }

    // ==========================================
    // 9. BINDINGS TOPIC
    // ==========================================

    @Bean
    public Binding topicEmail(
            @Qualifier("emailQueue") Queue queue,
            @Qualifier("topicExchange") TopicExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with("email.*");
    }

    @Bean
    public Binding topicStock(
            @Qualifier("stockQueue") Queue queue,
            @Qualifier("topicExchange") TopicExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with("stock.#");
    }

    @Bean
    public Binding topicInvoice(
            @Qualifier("invoiceQueue") Queue queue,
            @Qualifier("topicExchange") TopicExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with("invoice.*");
    }

    // ==========================================
    // 10. BINDINGS DLQ
    // ==========================================

    @Bean
    public Binding deadEmail(
            @Qualifier("emailDlq") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY_EMAIL);
    }

    @Bean
    public Binding deadStock(
            @Qualifier("stockDlq") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY_STOCK);
    }

    @Bean
    public Binding deadInvoice(
            @Qualifier("invoiceDlq") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {

        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY_INVOICE);
    }
}
