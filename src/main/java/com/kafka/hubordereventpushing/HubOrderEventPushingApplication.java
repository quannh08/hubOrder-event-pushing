package com.kafka.hubordereventpushing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HubOrderEventPushingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubOrderEventPushingApplication.class, args);
    }

}
