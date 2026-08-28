package com.example.sclms_notification.consumer;

import com.example.sclms_notification.dto.NotificationEvent;
import com.example.sclms_notification.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("NotificationConsumer BEAN INITIALIZED");
        log.info("========================================");
    }

    @KafkaListener(topics = "notification-events", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleNotification(NotificationEvent event, Acknowledgment ack) {
        try {
            log.info("Received notification event: type={}, recipient={}", event.getType(), event.getRecipient());
            System.out.println("Received notification event: type={}, recipient={}" + event.getType());
            switch (event.getType()) {
                case "SCHOOL_REGISTERED" -> emailService.sendSchoolRegisteredEmail(event);
                case "SCHOOL_APPROVED" -> emailService.sendSchoolApprovedEmail(event);
                case "PASSWORD_RESET" -> emailService.sendPasswordResetEmail(event);
                default -> log.warn("Unknown notification type: {}", event.getType());
            }
            ack.acknowledge();
        } catch (Exception e) {
            System.out.println("Error processing Kafka message: {}"+e.getMessage());
            // Don't acknowledge - message will be retried
            throw new RuntimeException("Failed to process notification", e);
        }
    }
}
