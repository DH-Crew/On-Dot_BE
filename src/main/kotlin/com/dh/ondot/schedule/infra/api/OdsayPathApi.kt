package com.dh.ondot.schedule.infra.api

import com.dh.ondot.core.config.OdsayApiConfig
import com.dh.ondot.schedule.infra.dto.OdsayErrorResponse
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse
import com.dh.ondot.schedule.infra.exception.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OdsayPathApi(
    private val odsayApiConfig: OdsayApiConfig,
    private val objectMapper: ObjectMapper,
    @Qualifier("odsayRestClient") private val restClient: RestClient,
) {

    @Retryable(
        retryFor = [OdsayServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchPublicTransportRoute(startX: Double, startY: Double, endX: Double, endY: Double): OdsayRouteApiResponse {
        try {
            val rawBody = restClient.get()
                .uri { builder ->
                    builder
                        .queryParam("apiKey", odsayApiConfig.apiKey)
                        .queryParam("SX", startX)
                        .queryParam("SY", startY)
                        .queryParam("EX", endX)
                        .queryParam("EY", endY)
                        .build(false)
                }
                .retrieve()
                .body(String::class.java)

            if (rawBody == null || rawBody.isBlank()) {
                throw OdsayUnhandledException("ODSay API 응답이 null 또는 비어 있습니다.")
            }

            if (rawBody.contains("\"error\"")) {
                val errRes = objectMapper.readValue(rawBody, OdsayErrorResponse::class.java)
                val err = errRes.error[0]
                throwOdsayExceptionByCode(err.code, err.message)
            }

            val routeRes = objectMapper.readValue(rawBody, OdsayRouteApiResponse::class.java)

            if (routeRes.result == null || routeRes.result.path == null) {
                throw OdsayServerErrorException("ODSay API 결과가 비어 있습니다.")
            }

            return routeRes
        } catch (ex: OdsayServerErrorException) {
            throw ex
        } catch (ex: OdsayTooCloseException) {
            throw ex
        } catch (e: Exception) {
            throw OdsayUnhandledException(e.message ?: "")
        }
    }

    private fun throwOdsayExceptionByCode(code: String, msg: String) {
        when (code) {
            "500" -> throw OdsayServerErrorException(msg)
            "-8" -> throw OdsayBadInputException(msg)
            "-9" -> throw OdsayMissingParamException(msg)
            "3", "4", "5" -> throw OdsayNoStopException(msg)
            "6" -> throw OdsayServiceAreaException(msg)
            "-98" -> throw OdsayTooCloseException(msg)
            "-99" -> throw OdsayNoResultException(msg)
            else -> throw OdsayUnhandledException(msg)
        }
    }
}
