package com.dh.ondot.notification.domain.service;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.notification.domain.EmergencyAlert;
import com.dh.ondot.notification.domain.SubwayAlert;
import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.dh.ondot.notification.domain.repository.EmergencyAlertRepository;
import com.dh.ondot.notification.domain.repository.SubwayAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final SubwayAlertRepository subwayAlertRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;

    @Transactional
    public void saveSubwayAlerts(
            LocalDate date,
            List<SubwayAlertDto> dtos
    ) {
        if (dtos.isEmpty()) return;

        Instant startOfDay = DateTimeUtils.toInstant(date.atStartOfDay());
        Instant nextDay = DateTimeUtils.toInstant(date.plusDays(1).atStartOfDay());

        Set<Instant> existing = subwayAlertRepository
                .findAllByCreatedAtBetween(startOfDay, nextDay)
                .stream()
                .map(SubwayAlert::getCreatedAt)
                .collect(Collectors.toSet());

        List<SubwayAlert> toSave = dtos.stream()
                .filter(dto -> !existing.contains(DateTimeUtils.toInstant(dto.createdAt())))
                .map(dto -> SubwayAlert.create(
                        dto.title(),
                        dto.content(),
                        dto.lineName(),
                        dto.startAt(),
                        dto.createdAt()
                ))
                .toList();

        if (!toSave.isEmpty()) {
            subwayAlertRepository.saveAll(toSave);
        }
    }

    @Transactional
    public void saveEmergencyAlerts(
            LocalDate date,
            List<EmergencyAlertDto> dtos
    ) {
        if (dtos.isEmpty()) return;

        Instant startOfDay = DateTimeUtils.toInstant(date.atStartOfDay());
        Instant nextDay = DateTimeUtils.toInstant(date.plusDays(1).atStartOfDay());

        Set<Instant> existing = emergencyAlertRepository
                .findAllByCreatedAtBetween(startOfDay, nextDay)
                .stream()
                .map(EmergencyAlert::getCreatedAt)
                .collect(Collectors.toSet());

        List<EmergencyAlert> toSave = dtos.stream()
                .filter(dto -> !existing.contains(DateTimeUtils.toInstant(dto.createdAt())))
                .map(dto -> EmergencyAlert.create(
                        dto.content(),
                        dto.regionName(),
                        dto.createdAt()
                ))
                .toList();

        if (!toSave.isEmpty()) {
            emergencyAlertRepository.saveAll(toSave);
        }
    }
}
