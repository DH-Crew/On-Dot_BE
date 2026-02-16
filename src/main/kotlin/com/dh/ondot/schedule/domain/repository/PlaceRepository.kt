package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.Place
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long>
