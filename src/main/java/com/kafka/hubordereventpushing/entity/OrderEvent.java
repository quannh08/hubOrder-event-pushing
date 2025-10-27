package com.kafka.hubordereventpushing.entity;

import com.kafka.hubordereventpushing.common.EventType;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "ORDER_EVENT")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_event_seq_gen")
    @SequenceGenerator(name = "order_event_seq_gen", sequenceName = "ORDER_EVENT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    Long id;

    @Column(name = "DATE_ONLY")
    Date dateOnly;

    @Column(name = "DATETIME")
    LocalDateTime dateTime;

    @Column(name = "AGENT_CODE")
    String agentCode;

    @Column(name = "SERVICE")
    @Nullable
    String service;

    @Column(name = "OBJECT_TYPE")
    String objectType;

    @Column(name = "ORDER_ID")
    Long orderId;

    @Column(name = "ORDER_SUB_ID")
    @Nullable
    Long orderSubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE")
    EventType eventType;

    @Column(name = "CONTENT")
    @Nullable
    String content;

    @Column(name = "AMOUNT")
    @Nullable
    BigDecimal amount;

    @Column(name = "AMOUNT_PRICE")
    @Nullable
    BigDecimal amountPrice;

    @Column(name = "AMOUNT_FEE")
    @Nullable
    BigDecimal amountFee;

    @Column(name = "AMOUNT_DISCOUNT")
    @Nullable
    BigDecimal amountDiscount;

    @Column(name = "DISCOUNT_CODE")
    @Nullable
    String discountCode;

    @Column(name = "PUSH_STATUS")
    Long pushStatus;

    @Column(name = "PUSH_DATETIME")
    @Nullable
    LocalDateTime pushDateTime;

    @Column(name = "PUSH_ERROR")
    @Nullable
    String pushError;
}
