package com.dh.ondot.schedule.infra.outbox;

public enum MessageStatus {
    INIT,
    SEND_SUCCESS,
    SEND_FAIL,
    DEAD,
}
