package com.financetracker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "transaction.exchange";
    public static final String QUEUE_NAME = "transaction.notifications";
    public static final String ROUTING_KEY = "transaction.created";

    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue transactionNotificationQueue() {
        return new Queue(QUEUE_NAME, true); // durable
    }

    @Bean
    public Binding transactionBinding(Queue transactionNotificationQueue,
                                      DirectExchange transactionExchange) {
        return BindingBuilder
                .bind(transactionNotificationQueue)
                .to(transactionExchange)
                .with(ROUTING_KEY);
    }
}
