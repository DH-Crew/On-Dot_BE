package com.dh.ondot.schedule.domain.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.SortedSet
import java.util.TreeSet

@Converter
class RepeatDaysConverter : AttributeConverter<SortedSet<Int>, String> {

    override fun convertToDatabaseColumn(attribute: SortedSet<Int>?): String {
        if (attribute.isNullOrEmpty()) return ""
        return attribute.joinToString(",")
    }

    override fun convertToEntityAttribute(dbData: String?): SortedSet<Int> {
        if (dbData.isNullOrBlank()) return TreeSet()
        return dbData.split(",")
            .map { it.trim().toInt() }
            .toCollection(TreeSet())
    }
}
