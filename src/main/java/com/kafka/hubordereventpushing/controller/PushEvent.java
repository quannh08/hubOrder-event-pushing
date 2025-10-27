package com.kafka.hubordereventpushing.controller;

import com.kafka.hubordereventpushing.service.KafkaPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PushEvent {
    private final KafkaPushService kafkaPushService;

    @PostMapping("/push")
    public void pushEvent(){
        kafkaPushService.submitEventToProcess();
    }
}
