package com.dh.ondot.schedule.fixture

import org.mockito.ArgumentMatchers
import org.mockito.Mockito

/**
 * Kotlin-safe Mockito argument matchers.
 *
 * Mockito's any() and eq() return null, which causes NullPointerException
 * when used with Kotlin non-null parameters. These helpers provide
 * non-null defaults while still registering the matcher with Mockito.
 */
object MockitoHelper {

    /**
     * Returns Mockito.any() as a non-null type T.
     * Uses a Kotlin-safe cast to avoid NPE on non-null parameters.
     */
    fun <T> anyNonNull(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    /**
     * Returns Mockito.eq(value) as a non-null type T.
     */
    fun <T> eqNonNull(value: T): T {
        ArgumentMatchers.eq(value)
        @Suppress("UNCHECKED_CAST")
        return value
    }
}
