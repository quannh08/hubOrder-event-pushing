package com.kafka.hubordereventpushing.repository;

import com.kafka.hubordereventpushing.entity.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent,Long> {
    @Query(value = """ 
            SELECT oe.*
            FROM ORDER_EVENT oe
            WHERE oe.PUSH_STATUS = 0
            ORDER BY oe.ID
            FETCH FIRST 10 ROWS ONLY
            """, nativeQuery = true)
    List<OrderEvent> findTop10UnprocessedForUpdate();

}
