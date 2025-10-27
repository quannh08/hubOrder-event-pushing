package com.kafka.hubordereventpushing.repository;

import com.kafka.hubordereventpushing.entity.OrderEventKafkaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventKafkaConfigRepository extends JpaRepository<OrderEventKafkaConfig,Long> {
    List<OrderEventKafkaConfig> findAll();

    @Query(value = """ 
            SELECT * 
            FROM ORDER_EVENT_KAFKA_CONFIG k 
            WHERE k.status = 0
            """, nativeQuery = true)
    List<OrderEventKafkaConfig> findActiveConfigs();
}
