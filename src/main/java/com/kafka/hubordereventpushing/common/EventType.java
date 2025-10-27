package com.kafka.hubordereventpushing.common;

public enum EventType {
    HOLD,HOLD_FAILED, PAID, COMPLETED, ISSUE_FAILED, REFUNDED,
    REFUND_REQUESTED, CANCELLED,EXPIRED, CC_receive,
    CC_transfer, CC_cancel, INVOICE_ISSUED

}
