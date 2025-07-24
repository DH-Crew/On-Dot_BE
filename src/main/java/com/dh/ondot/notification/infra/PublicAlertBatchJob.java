package com.dh.ondot.notification.infra;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.notification.domain.service.AlertService;
import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.dh.ondot.notification.domain.service.EmergencyAlertService;
import com.dh.ondot.notification.domain.service.SubwayAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicAlertBatchJob {
    private static final String EVERY_20_MINUTES = "0 0/20 * * * *";

    private final AlertService alertService;
    private final SubwayAlertService subwayAlertService;
    private final EmergencyAlertService emergencyAlertService;

    @Scheduled(cron = EVERY_20_MINUTES)
    public void refreshPublicAlerts() {
        LocalDate today = DateTimeUtils.nowSeoulDate();
        try {
            List<SubwayAlertDto> subwayAlertDtoList = subwayAlertService.fetchAlertsByDate(today);
            alertService.saveSubwayAlerts(today, subwayAlertDtoList);
            List<EmergencyAlertDto> emergencyAlertDtoList = emergencyAlertService.fetchAlertsByDate(today);
            alertService.saveEmergencyAlerts(today, emergencyAlertDtoList);
        } catch (Exception e) {
            log.error("Failed to update public alert data", e);
        }
    }
}
