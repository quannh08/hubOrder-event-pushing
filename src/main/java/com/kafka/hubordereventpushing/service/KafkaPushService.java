package com.kafka.hubordereventpushing.service;

import com.kafka.hubordereventpushing.entity.OrderEvent;
import com.kafka.hubordereventpushing.repository.OrderEventKafkaConfigRepository;
import com.kafka.hubordereventpushing.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "KAFKA-PUSH-SERVICE")
//@RequiredArgsConstructor
public class KafkaPushService {

    private final BatchMessageService batchMessageService;

    private final ConfigLoader configLoader;

    @Qualifier("customExecutor")
    private final ThreadPoolTaskExecutor executor;

    public KafkaPushService(
            BatchMessageService batchMessageService,
            ConfigLoader configLoader,
            @Qualifier("customExecutor") ThreadPoolTaskExecutor executor) {
        this.batchMessageService = batchMessageService;
        this.configLoader = configLoader;
        this.executor = executor;
    }


    public void submitEventToProcess(){
        while(true){
            executor.submit(() -> {
                try{
                    log.info("Step1:");
                    batchMessageService.processEvent(configLoader.getKafkaConfigs());
                }
                catch (Exception e){
                    log.error(e.getMessage());
                }
            });

        }
    }
}
