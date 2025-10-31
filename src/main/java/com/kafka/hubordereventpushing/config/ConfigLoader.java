package com.kafka.hubordereventpushing.config;

import com.kafka.hubordereventpushing.entity.Config;
import com.kafka.hubordereventpushing.entity.OrderEventKafkaConfig;
import com.kafka.hubordereventpushing.repository.ConfigRepository;
import com.kafka.hubordereventpushing.repository.OrderEventKafkaConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Configuration
@Slf4j(topic = "CONFIG-LOADER")
@RequiredArgsConstructor
@Data
public class ConfigLoader {

    private final OrderEventKafkaConfigRepository orderEventKafkaConfigRepository;

    private final ConfigRepository configRepository;

    private static String event_kafka_server;
    private static String event_telegram_key;
    private static String event_telegram_group_id;

    private static List<OrderEventKafkaConfig> kafka_configs;

    public static List<OrderEventKafkaConfig> getKafkaConfigs() {
        log.info("getKafkaConfigs:"+ kafka_configs);
        return kafka_configs;
    }

    public static String getEventKafkaServer() {
        return event_kafka_server;
    }

    public static String getEventTelegramKey() {
        return event_telegram_key;
    }

    public static String getEventTelegramGroupId() {
        return event_telegram_group_id;
    }

    @PostConstruct
    public void init() {
        loadConfigs();
        log.info("kafka server: " + event_kafka_server);
        log.info("telegram key: " + event_telegram_key);
        log.info("telegram group id: " + event_telegram_group_id);
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void reload() {
        loadConfigs();
    }

    private void loadConfigs() {
        kafka_configs = orderEventKafkaConfigRepository.findActiveConfigs();
        log.info("Loading kafka configs :{}", kafka_configs);

        event_kafka_server = configRepository.findByKey("event_kafka_server")
                .map(Config::getValue).orElse(null);

        event_telegram_key = configRepository.findByKey("event_telegram_key")
                .map(Config::getValue).orElse(null);

        event_telegram_group_id = configRepository.findByKey("event_telegram_group_id")
                .map(Config::getValue).orElse(null);
    }



}
