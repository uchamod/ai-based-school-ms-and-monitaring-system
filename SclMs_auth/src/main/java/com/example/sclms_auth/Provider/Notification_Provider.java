package com.example.sclms_auth.Provider;


import com.example.sclms_auth.DTO.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Notification_Provider {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
   private static final String topic = "notification-events";

    @Async("notificationExecutor")
   public void sendNotification(NotificationEvent event) {
       kafkaTemplate.send(topic, event).whenComplete(
               (result, ex) -> {
                   if (ex == null) {
                       System.out.println("✅ Kafka send success: " + result.getRecordMetadata());
                   } else {
                       System.out.println("❌ Kafka send failed: " + ex.getMessage());
                   }
               }
       );
       System.out.println("Sent inventory update for product: " + event.getType());

   }
}
