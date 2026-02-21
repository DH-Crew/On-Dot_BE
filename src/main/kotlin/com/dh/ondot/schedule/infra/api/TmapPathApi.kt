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

@Component
class TmapPathApi(
    @Qualifier("tmapRestClient") private val tmapRestClient: RestClient,
) {
    @Retryable(
        retryFor = [TmapServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchCarRoute(startX: Double, startY: Double, endX: Double, endY: Double): TmapRouteApiResponse {
        try {
            val response = tmapRestClient.post()
                .uri("/tmap/routes?version=1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf(
                    "startX" to startX.toString(),
                    "startY" to startY.toString(),
                    "endX" to endX.toString(),
                    "endY" to endY.toString(),
                    "reqCoordType" to "WGS84GEO",
                    "resCoordType" to "WGS84GEO",
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
                throw TmapServerErrorException(ex.message ?: "")
            }
            throw TmapUnhandledException(ex.message ?: "")
        } catch (e: Exception) {
            throw TmapUnhandledException(e.message ?: "")
        }
    }
}
