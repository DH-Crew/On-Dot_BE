package com.dh.ondot.notification.domain.service;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.notification.domain.EmergencyAlert;
import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.dh.ondot.notification.domain.repository.EmergencyAlertRepository;
import com.dh.ondot.notification.infra.emergency.EmergencyAlertApi;
import com.dh.ondot.notification.infra.emergency.EmergencyAlertDtoMapper;
import com.dh.ondot.notification.infra.emergency.EmergencyAlertJsonExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyAlertService {
    private final EmergencyAlertApi emergencyAlertApi;
    private final EmergencyAlertJsonExtractor emergencyAlertJsonExtractor;
    private final EmergencyAlertDtoMapper emergencyAlertDtoMapper;
    private final EmergencyAlertRepository emergencyAlertRepository;

    public List<EmergencyAlertDto> fetchAlertsByDate(LocalDate date) {
        String rawJson = emergencyAlertApi.fetchAlertsByDate(date);
        JsonNode alertsNode = emergencyAlertJsonExtractor.extractAlerts(rawJson);
        return emergencyAlertDtoMapper.toDto(alertsNode);
    }

    @Transactional(readOnly = true)
    public String getIssuesByAddress(String roadAddress) {
        String regionKey = extractRegionKey(roadAddress);
        String provinceKey = extractProvince(roadAddress);
        String allRegion = provinceKey + " 전체";

        LocalDate today = TimeUtils.nowSeoulDate();
        Instant from = TimeUtils.toInstant(today.atStartOfDay());
        Instant to = TimeUtils.toInstant(today.plusDays(1).atStartOfDay());
        List<EmergencyAlert> alerts = emergencyAlertRepository.findAllByCreatedAtBetween(from, to);

        List<String> contents = alerts.stream()
                .filter(a -> matchesRegionCSV(a.getRegionName(), regionKey, allRegion))
                .map(EmergencyAlert::getContent)
                .collect(Collectors.toList());

        return String.join("\n", contents);
    }

    // “서울특별시 동작구 흑석로23-2” → “서울특별시 동작구”
    private String extractRegionKey(String roadAddress) {
        String[] tok = roadAddress.split("\\s+");
        if (tok.length >= 2) return tok[0] + " " + tok[1];
        if (tok.length == 1) return tok[0];
        return "";
    }

    // “서울특별시 동작구 흑석로23-2” → “서울특별시”
    private String extractProvince(String roadAddress) {
        String[] tok = roadAddress.split("\\s+");
        return tok.length >= 1 ? tok[0] : "";
    }

    private boolean matchesRegionCSV(String regionCsv, String regionKey, String allRegion) {
        for (String part : regionCsv.split(",")) {
            String r = part.trim();
            if (r.equals(regionKey) || r.equals(allRegion)) {
                return true;
            }
        }
        return false;
    }
}
