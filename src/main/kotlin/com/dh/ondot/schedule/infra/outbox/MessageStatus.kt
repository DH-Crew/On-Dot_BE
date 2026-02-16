package com.dh.ondot.schedule.infra.outbox

enum class MessageStatus {
    INIT,
    SEND_SUCCESS,
    SEND_FAIL,
    DEAD,
}
