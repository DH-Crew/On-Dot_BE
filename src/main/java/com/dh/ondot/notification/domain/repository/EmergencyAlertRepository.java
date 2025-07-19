package com.dh.ondot.notification.domain.repository;

import com.dh.ondot.notification.domain.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert,Long> {
    List<EmergencyAlert> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
