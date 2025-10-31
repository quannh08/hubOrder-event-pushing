package com.kafka.hubordereventpushing;

import com.kafka.hubordereventpushing.service.KafkaPushService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HubOrderEventPushingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubOrderEventPushingApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(KafkaPushService kafkaPushService) {
        return args -> {
            kafkaPushService.submitEventToProcess();
        };
    }
}
