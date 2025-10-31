package com.kafka.hubordereventpushing.service;

import com.google.gson.Gson;
import com.kafka.hubordereventpushing.entity.OrderEvent;
import com.kafka.hubordereventpushing.entity.OrderEventKafkaConfig;
import com.kafka.hubordereventpushing.repository.OrderEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "BATCH-MESSAGE-SERVICE")
@RequiredArgsConstructor
public class BatchMessageService {

    private final OrderEventRepository orderEventRepository;

    private final ProducerService producerService;

    private final Gson gson;

    // Get 10 event unprocess for update
    public List<OrderEvent> preprocessing(List<OrderEvent> events) {

        if (events.isEmpty()) {
            try {
                log.warn("No order events to process");
                Thread.sleep(5000);
                return List.of();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return List.of();
            }
        }
        return events;
    }

    public void processEvent(List<OrderEventKafkaConfig> eventKafkaConfigs, List<OrderEvent> orderEvents) {

        List<OrderEvent> eventToFilter = preprocessing(orderEvents);
        log.info("filter events in order_event_kafka_config");
        for (OrderEvent event : eventToFilter) {
            // Duyệt cấu hình đẩy Kafka
            Optional<OrderEventKafkaConfig> matchedConfig = eventKafkaConfigs.stream()
                    .filter(config ->
                                    (config.getService().equals("*") || config.getService().equals(event.getService())) &&
                                            (config.getAgentCode().equals("*") || config.getAgentCode().equals(event.getAgentCode())) &&
                                            (config.getObjectType().equals("*") || config.getObjectType().equals(event.getObjectType())) &&
                                            (config.getEventType().equals("*") || config.getEventType().equals(event.getEventType().toString()))
                    ).findFirst();
            if (matchedConfig.isEmpty()) {
                // Nếu không có cấu hình phù hợp, gán push_status = 9
                log.warn("Event with id {} not processed", event.getId());
                event.setPushStatus(9L);
                orderEventRepository.save(event);
                continue;
            } else {
                String topic = matchedConfig.get().getKafkaTopic();
                log.info("Event with id {} processed", event.getId());
                producerService.createTopicIfMissing(topic);
                producerService.pushEvent(topic, event.getId(), gson.toJson(event));
            }

        }
    }

}
