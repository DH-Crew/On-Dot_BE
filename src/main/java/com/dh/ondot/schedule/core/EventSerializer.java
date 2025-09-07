package com.dh.ondot.schedule.core;

import com.dh.ondot.schedule.core.exception.SerializationException;

public interface EventSerializer {
    String serialize(Object event) throws SerializationException;
}
