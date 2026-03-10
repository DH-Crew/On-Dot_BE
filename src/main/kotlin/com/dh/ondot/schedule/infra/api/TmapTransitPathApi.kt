package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.infra.dto.TmapTransitErrorResponse
import com.dh.ondot.schedule.infra.dto.TmapTransitRouteApiResponse
import com.dh.ondot.schedule.infra.exception.TmapTransitBadInputException
import com.dh.ondot.schedule.infra.exception.TmapTransitMissingParamException
import com.dh.ondot.schedule.infra.exception.TmapTransitNoRouteException
import com.dh.ondot.schedule.infra.exception.TmapTransitServerErrorException
import com.dh.ondot.schedule.infra.exception.TmapTransitServiceAreaException
import com.dh.ondot.schedule.infra.exception.TmapTransitUnhandledException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class TmapTransitPathApi(
    @Qualifier("tmapRestClient") private val tmapRestClient: RestClient,
    private val objectMapper: ObjectMapper,
) {

    @Retryable(
        retryFor = [TmapTransitServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchTransitRoute(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
    ): TmapTransitRouteApiResponse {
        try {
            val rawBody = tmapRestClient.post()
                .uri("/transit/routes/sub")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf(
                    "startX" to startX.toString(),
                    "startY" to startY.toString(),
                    "endX" to endX.toString(),
                    "endY" to endY.toString(),
                    "count" to 10,
                    "lang" to 0,
                    "format" to "json",
                ))
                .retrieve()
                .body(String::class.java)

            if (rawBody == null || rawBody.isBlank()) {
                throw TmapTransitUnhandledException("TMAP Transit API 응답이 null 또는 비어 있습니다.")
            }

            if (rawBody.contains("\"result\"") && rawBody.contains("\"status\"")) {
                val errorResponse = objectMapper.readValue(rawBody, TmapTransitErrorResponse::class.java)
                val result = errorResponse.result
                if (result != null) {
                    throwExceptionByErrorCode(result.status, result.message)
                }
            }

            val response = objectMapper.readValue(rawBody, TmapTransitRouteApiResponse::class.java)

            if (response.metaData.plan.itineraries.isEmpty()) {
                throw TmapTransitNoRouteException("출발지($startX,$startY) → 도착지($endX,$endY) 경로 없음")
            }

            return response
        } catch (ex: TmapTransitServerErrorException) {
            throw ex
        } catch (ex: TmapTransitNoRouteException) {
            throw ex
        } catch (ex: TmapTransitBadInputException) {
            throw ex
        } catch (ex: TmapTransitMissingParamException) {
            throw ex
        } catch (ex: TmapTransitServiceAreaException) {
            throw ex
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.is5xxServerError) {
                throw TmapTransitServerErrorException("${ex.statusCode}: ${ex.message}")
            }
            throw TmapTransitUnhandledException("${ex.statusCode}: ${ex.message}")
        } catch (e: Exception) {
            throw TmapTransitUnhandledException("${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun throwExceptionByErrorCode(status: Int, message: String) {
        when (status) {
            11, 12, 13, 14 -> throw TmapTransitNoRouteException(message)
            21 -> throw TmapTransitBadInputException(message)
            22 -> throw TmapTransitMissingParamException(message)
            23 -> throw TmapTransitServiceAreaException(message)
            31 -> throw TmapTransitServerErrorException(message)
            32 -> throw TmapTransitUnhandledException(message)
            else -> throw TmapTransitUnhandledException("Unknown error code $status: $message")
        }
    }
}
