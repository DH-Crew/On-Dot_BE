package com.dh.ondot.schedule.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Converter
public class RepeatDaysConverter implements AttributeConverter<SortedSet<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(SortedSet<Integer> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public SortedSet<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new TreeSet<>();
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
