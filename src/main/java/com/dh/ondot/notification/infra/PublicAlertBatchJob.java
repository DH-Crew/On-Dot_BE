package com.dh.ondot.notification.infra;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.notification.domain.service.AlertService;
import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.dh.ondot.notification.infra.api.EmergencyAlertApi;
import com.dh.ondot.notification.infra.api.SubwayAlertApi;
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

    private final SubwayAlertApi subwayAlertApi;
    private final EmergencyAlertApi emergencyAlertApi;
    private final AlertService alertService;

    @Scheduled(cron = EVERY_20_MINUTES)
    public void refreshPublicAlerts() {
        LocalDate today = DateTimeUtils.nowSeoulDate();
        try {
            List<SubwayAlertDto> subwayDtos = subwayAlertApi.fetchAllAlertsByDate(today);
            alertService.saveSubwayAlerts(today, subwayDtos);
            List<EmergencyAlertDto> emergencyDtos = emergencyAlertApi.fetchAllAlertsByDate(today);
            alertService.saveEmergencyAlerts(today, emergencyDtos);
        } catch (Exception e) {
            log.error("Failed to update public alert data", e);
        }
    }
}
