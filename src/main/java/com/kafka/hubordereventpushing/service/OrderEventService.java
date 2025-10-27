package com.kafka.hubordereventpushing.service;

import com.kafka.hubordereventpushing.entity.OrderEvent;
import com.kafka.hubordereventpushing.exception.ResourceNotFoundException;
import com.kafka.hubordereventpushing.repository.OrderEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-EVENT-SERVICE")
public class OrderEventService {

    private final OrderEventRepository orderEventRepository;

    @Transactional
    public OrderEvent updatePushStatus(Long orderEventId, Long push_status) {
        OrderEvent event = orderEventRepository.findById(orderEventId)
                .orElseThrow(() ->  new ResourceNotFoundException("Order Event not found with id :"+orderEventId));
        log.info("update to "+event.toString());
        event.setPushStatus(push_status);
        return orderEventRepository.save(event);
    }
}
