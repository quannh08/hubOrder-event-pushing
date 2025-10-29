package com.kafka.hubordereventpushing.repository;

import com.kafka.hubordereventpushing.entity.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent,Long> {
    @Query(value = """ 
    SELECT * FROM ORDER_EVENT oe 
        WHERE oe.PUSH_STATUS = 0 
        AND oe.ROWID IN ( 
            SELECT ROWID FROM ( 
                SELECT ROWID 
                    FROM ORDER_EVENT 
                    WHERE PUSH_STATUS = 0 
                    ORDER BY ID ) 
                WHERE ROWNUM <= 10 ) 
            FOR UPDATE SKIP LOCKED """, nativeQuery = true)
    List<OrderEvent> findTop10UnprocessedForUpdate();

}
