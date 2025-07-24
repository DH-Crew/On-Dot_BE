package com.dh.ondot.notification.domain.service;

import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.dh.ondot.notification.infra.subway.SubwayAlertDtoMapper;
import com.dh.ondot.notification.infra.subway.SubwayAlertJsonExtractor;
import com.dh.ondot.notification.infra.subway.SubwayAlertApi;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubwayAlertService {
    private final SubwayAlertApi subwayAlertApi;
    private final SubwayAlertJsonExtractor subwayAlertJsonExtractor;
    private final SubwayAlertDtoMapper subwayAlertDtoMapper;

    public List<SubwayAlertDto> fetchAlertsByDate(LocalDate date) {
        String rawJson = subwayAlertApi.getRawAlertsByDate(date);
        JsonNode alertsNode = subwayAlertJsonExtractor.extractAlerts(rawJson);

        if (!alertsNode.isArray()) {
            return Collections.emptyList();
        }

        List<SubwayAlertDto> list = new ArrayList<>();
        for (JsonNode node : alertsNode) {
            try {
                list.add(subwayAlertDtoMapper.toDto(node));
            } catch (Exception ex) {
                log.warn("Failed to toDto subway alert JSON node -> DTO, node={}", node.toString(), ex);
            }
        }
        return list;
    }
}
