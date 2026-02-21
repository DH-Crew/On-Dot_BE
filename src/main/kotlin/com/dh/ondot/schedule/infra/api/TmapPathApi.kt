package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.infra.dto.TmapRouteApiResponse
import com.dh.ondot.schedule.infra.exception.TmapNoResultException
import com.dh.ondot.schedule.infra.exception.TmapServerErrorException
import com.dh.ondot.schedule.infra.exception.TmapUnhandledException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
class TmapPathApi(
    @Qualifier("tmapRestClient") private val tmapRestClient: RestClient,
) {
    @Retryable(
        retryFor = [TmapServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchCarRoute(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        appointmentAt: LocalDateTime? = null,
    ): TmapRouteApiResponse {
        val kst = ZoneId.of("Asia/Seoul")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

        val predictionType: String
        val predictionTime: String
        if (appointmentAt != null) {
            predictionType = "arrival"
            predictionTime = appointmentAt.atZone(kst).format(formatter)
        } else {
            predictionType = "departure"
            predictionTime = ZonedDateTime.now(kst).format(formatter)
        }

        try {
            val response = tmapRestClient.post()
                .uri("/tmap/routes/prediction?version=1&resCoordType=WGS84GEO&reqCoordType=WGS84GEO&sort=index")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf(
                    "routesInfo" to mapOf(
                        "departure" to mapOf(
                            "name" to "departure",
                            "lon" to startX.toString(),
                            "lat" to startY.toString(),
                        ),
                        "destination" to mapOf(
                            "name" to "destination",
                            "lon" to endX.toString(),
                            "lat" to endY.toString(),
                        ),
                        "predictionType" to predictionType,
                        "predictionTime" to predictionTime,
                    ),
                ))
                .retrieve()
                .body(TmapRouteApiResponse::class.java)

            if (response == null || response.features.isNullOrEmpty()) {
                throw TmapNoResultException("출발지($startX,$startY) → 도착지($endX,$endY)")
            }

            return response
        } catch (ex: TmapServerErrorException) {
            throw ex
        } catch (ex: TmapNoResultException) {
            throw ex
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.is5xxServerError) {
                throw TmapServerErrorException("${ex.statusCode}: ${ex.message}")
            }
            throw TmapUnhandledException("${ex.statusCode}: ${ex.message}")
        } catch (e: Exception) {
            throw TmapUnhandledException("${e.javaClass.simpleName}: ${e.message}")
        }
    }
}
