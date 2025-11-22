package com.dh.ondot.notification.infra.emergency;

import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class EmergencyAlertDtoMapper {
    private static final DateTimeFormatter ISSUED_AT_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public List<EmergencyAlertDto> toDto(JsonNode bodyNode) {
        if (!bodyNode.isArray()) {
            return Collections.emptyList();
        }
        List<EmergencyAlertDto> list = new ArrayList<>(bodyNode.size());
        for (JsonNode node : bodyNode) {
            String content = node.path("MSG_CN").asText("");
            String region = node.path("RCPTN_RGN_NM").asText("");
            String crtDt = node.path("CRT_DT").asText("");
            LocalDateTime issuedAt = LocalDateTime.parse(crtDt, ISSUED_AT_FMT);
            list.add(new EmergencyAlertDto(content, region, issuedAt));
        }
        return list;
    }
}
