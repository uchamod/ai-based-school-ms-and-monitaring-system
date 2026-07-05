package com.example.sclms_notification.consumer;

import com.example.sclms_notification.dto.NotificationEvent;
import com.example.sclms_notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "notification-events", groupId = "${spring.kafka.consumer.group-id}")
    public void handleNotification(NotificationEvent event) {
        log.info("Received notification event: type={}, recipient={}", event.getType(), event.getRecipient());
        System.out.println("Received notification event: type={}, recipient={}"+event.getType());
        switch (event.getType()) {
            case "SCHOOL_REGISTERED" -> emailService.sendSchoolRegisteredEmail(event);
            case "SCHOOL_APPROVED" -> emailService.sendSchoolApprovedEmail(event);
            case "PASSWORD_RESET" -> emailService.sendPasswordResetEmail(event);
            default -> log.warn("Unknown notification type: {}", event.getType());
        }
    }
}
