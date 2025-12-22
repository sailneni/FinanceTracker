package com.financetracker.publisher;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.TransactionNotificationRequest;
import com.financetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Autowired
    private ServiceBusSenderClient senderClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    public void publishTransactionEvent(TransactionNotificationRequest request) {
        //rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, request);
        try {
            String json = objectMapper.writeValueAsString(request);
            senderClient.sendMessage(new ServiceBusMessage(json));
            log.info("Sent transaction notification to Service Bus for tx {}", request);
        } catch (Exception ex) {
            log.error("Failed to send transaction notification to Service Bus", ex);
        }
        System.out.println("Published transaction event to RabbitMQ: " + request);
    }
}
