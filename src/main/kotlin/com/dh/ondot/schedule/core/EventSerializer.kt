package com.dh.ondot.schedule.core

interface EventSerializer {
    fun serialize(event: Any): String
}
