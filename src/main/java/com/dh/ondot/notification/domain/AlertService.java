package com.dh.ondot.notification.domain;

import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.dh.ondot.notification.domain.repository.EmergencyAlertRepository;
import com.dh.ondot.notification.domain.repository.SubwayAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextDay = date.plusDays(1).atStartOfDay();

        Set<LocalDateTime> existing = subwayAlertRepository
                .findAllByCreatedAtBetween(startOfDay, nextDay)
                .stream()
                .map(SubwayAlert::getCreatedAt)
                .collect(Collectors.toSet());

        List<SubwayAlert> toSave = dtos.stream()
                .filter(dto -> !existing.contains(dto.createdAt()))
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

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextDay = date.plusDays(1).atStartOfDay();

        Set<LocalDateTime> existing = emergencyAlertRepository
                .findAllByCreatedAtBetween(startOfDay, nextDay)
                .stream()
                .map(EmergencyAlert::getCreatedAt)
                .collect(Collectors.toSet());

        List<EmergencyAlert> toSave = dtos.stream()
                .filter(dto -> !existing.contains(dto.createdAt()))
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
