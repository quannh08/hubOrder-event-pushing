package com.kafka.hubordereventpushing.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;



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
                    batchMessageService.processEvent(configLoader.getKafkaConfigs());
                }
                catch (Exception e){
                    log.error(e.getMessage());
                }
            });

        }
    }
}
