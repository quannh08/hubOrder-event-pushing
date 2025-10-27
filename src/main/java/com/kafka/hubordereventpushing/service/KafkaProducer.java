package com.kafka.hubordereventpushing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
//@RequiredArgsConstructor
@Slf4j(topic = "PRODUCER-SERVICE")
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OrderEventService orderEventService;

    private final SendToTelegram sendToTelegram;

    private final KafkaAdmin kafkaAdmin;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Qualifier("telegramExecutor")
    private final ThreadPoolTaskExecutor executor;

    public KafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            OrderEventService orderEventService,
            SendToTelegram sendToTelegram, KafkaAdmin kafkaAdmin,
            @Qualifier("telegramExecutor") ThreadPoolTaskExecutor executor) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventService = orderEventService;
        this.sendToTelegram = sendToTelegram;
        this.kafkaAdmin = kafkaAdmin;
        this.executor = executor;
    }

    public void pushEvent(String topic,Long eventId, String request) {
        log.info("send Transaction");
        log.info("Step 3");
        kafkaTemplate.send(topic, String.valueOf(eventId), request)
        .whenComplete((result, ex) -> {
            if (ex == null) {
                // gửi thành công
                log.info("Sent successfully to partition {}", result.getRecordMetadata().partition());
                try {
                    orderEventService.updatePushStatus(eventId, 1L);
                    log.info("update to 1"); // Chỉ in khi thành công
                } catch (Exception e) {
                    log.error("Failed to update push status for eventId: {}", eventId, e);
                    // Có thể throw lại hoặc xử lý tiếp
                }
                log.info("update to 1");
            } else {
                // gửi thất bại

                log.info("Fail update to 2");

                orderEventService.updatePushStatus(eventId,2L);
                executor.submit(() -> {
                    try{
                        sendToTelegram.sendError(ex,eventId);
                    } catch (Exception e) {
                        log.error("send error", e);
                    }

                });
                 //Nghỉ 10s TRƯỚC KHI CHO PHÉP GỬI LẠI (nếu cần retry)
                scheduler.schedule(() -> {
                    log.info("sleep 10s");
                }, 10, TimeUnit.SECONDS);
            }
        });
    }


    public void createTopicIfMissing(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> existing = adminClient.listTopics().names().get();
            if (!existing.contains(topicName)) {
                NewTopic topic = TopicBuilder.name(topicName)
                        .partitions(3)
                        .replicas(2)
                        .config("retention.ms", "604800000")
                        .build();
                adminClient.createTopics(Collections.singleton(topic));
                log.info("Created new topic: {}", topicName);
            }
        } catch (Exception e) {
            log.error("Failed to create topic {}: {}", topicName, e.getMessage());
        }
    }
}
