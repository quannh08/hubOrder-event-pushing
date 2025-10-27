package com.kafka.hubordereventpushing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "ORDER_EVENT_KAFKA_CONFIG")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderEventKafkaConfig {

    @Column(name = "ID")
    @Id
    Long id;

    @Column(name = "SERVICE")
    String service;

    @Column(name = "AGENT_CODE")
    String agentCode;

    @Column(name = "OBJECT_TYPE")
    String objectType;

    @Column(name = "EVENT_TYPE")
    String eventType;

    @Column(name = "KAFKA_TOPIC")
    String kafkaTopic;

    @Column(name = "STATUS")
    Long status;
}
