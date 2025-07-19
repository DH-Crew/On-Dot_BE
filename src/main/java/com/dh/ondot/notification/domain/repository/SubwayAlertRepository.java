package com.dh.ondot.notification.domain.repository;

import com.dh.ondot.notification.domain.SubwayAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubwayAlertRepository extends JpaRepository<SubwayAlert,Long> {
    List<SubwayAlert> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
