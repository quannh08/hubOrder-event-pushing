package com.kafka.hubordereventpushing.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;


@Service
//@RequiredArgsConstructor
@Slf4j(topic = "PRODUCER-SERVICE")
public class ProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OrderEventService orderEventService;

    private final SendToTelegram sendToTelegram;

    private final KafkaAdmin kafkaAdmin;

//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Qualifier("telegramExecutor")
    private final ThreadPoolTaskExecutor executor;

    public ProducerService(
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
        log.info("Event with id {} send to topic {}", eventId, topic);
        kafkaTemplate.send(topic, String.valueOf(eventId), request)
        .whenComplete((result, ex) -> {
            if (ex == null) {
                // gửi thành công
                log.info("Sent successfully to partition {}", result.getRecordMetadata().partition());
                try {
                    orderEventService.updatePushStatus(eventId, 1L);
                    log.info("Event with id: {} update status to 1", eventId); // Chỉ in khi thành công
                } catch (Exception e) {
                    log.error("Failed to update push status for eventId: {}", eventId, e);
                    // Có thể throw lại hoặc xử lý tiếp
                }
            } else {
                // gửi thất bại

                log.info("Fail update status to 2");

                orderEventService.updatePushStatus(eventId,2L);
                orderEventService.updatePushError(eventId, ex.getMessage());
                executor.submit(() -> {
                    try{
                        log.warn("Sent error to Tele");
                        sendToTelegram.sendError(ex,eventId);
                    } catch (Exception e) {
                        log.error("send error", e);
                    }

                });
                 //Nghỉ 10s TRƯỚC KHI CHO PHÉP GỬI LẠI
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.error("Interrupted", e);
                }
//                scheduler.schedule(() -> {
//                    log.info("sleep 10s");
//                }, 10, TimeUnit.SECONDS);
            }
        });
    }


    public void createTopicIfMissing(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> existing = adminClient.listTopics().names().get();
            if (!existing.contains(topicName)) {
                NewTopic topic = TopicBuilder.name(topicName)
                        .partitions(3)
                        .replicas(1)
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
