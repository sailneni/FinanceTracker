package com.financetracker.publisher;

import com.financetracker.dto.TransactionNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.financetracker.config.RabbitMQConfig.EXCHANGE_NAME;
import static com.financetracker.config.RabbitMQConfig.ROUTING_KEY;

@Service
@RequiredArgsConstructor
public class TransactionEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishTransactionEvent(TransactionNotificationRequest request) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, request);
        System.out.println("Published transaction event to RabbitMQ: " + request);
    }
}
