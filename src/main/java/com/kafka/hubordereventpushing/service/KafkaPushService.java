package com.kafka.hubordereventpushing.service;


import com.kafka.hubordereventpushing.config.ConfigLoader;
import com.kafka.hubordereventpushing.entity.OrderEvent;
import com.kafka.hubordereventpushing.repository.OrderEventRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j(topic = "KAFKA-PUSH-SERVICE")
//@RequiredArgsConstructor
public class KafkaPushService {

    private final BatchMessageService batchMessageService;

    private final ConfigLoader configLoader;

    private final OrderEventRepository orderEventRepository;

    @Qualifier("customExecutor")
    private final ThreadPoolTaskExecutor executor;


    public KafkaPushService(
            BatchMessageService batchMessageService,
            ConfigLoader configLoader, OrderEventRepository orderEventRepository,
            @Qualifier("customExecutor") ThreadPoolTaskExecutor executor) {
        this.batchMessageService = batchMessageService;
        this.configLoader = configLoader;
        this.orderEventRepository = orderEventRepository;
        this.executor = executor;
    }


    @Transactional
    public List<OrderEvent> getEventToProcess(){
        log.info("get 10 Event unprocessed to update");
        List<OrderEvent> events = orderEventRepository.findTop10UnprocessedForUpdate();
        if (!events.isEmpty()) {
            // Gán push_status = 5 và push_datetime = thời điểm hiện tại
            events.forEach(event -> {
                event.setPushStatus(5L);
                event.setPushDateTime(LocalDateTime.now());
            });
            orderEventRepository.saveAll(events);
        }
        return events;
    }

    public void submitEventToProcess(){
        while(true){

            List<OrderEvent> events = orderEventRepository.findTop10UnprocessedForUpdate();

            executor.submit(() -> {
                try{
                    batchMessageService.processEvent(configLoader.getKafkaConfigs(), events);
                }
                catch (Exception e){
                    log.error(e.getMessage());
                }
            });

        }
    }
}
