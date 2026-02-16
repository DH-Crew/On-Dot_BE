package com.dh.ondot.schedule.infra.api

import com.dh.ondot.core.config.OdsayApiConfig
import com.dh.ondot.schedule.infra.dto.OdsayErrorResponse
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse
import com.dh.ondot.schedule.infra.exception.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URI

@Component
class OdsayPathApi(
    private val odsayApiConfig: OdsayApiConfig,
    private val objectMapper: ObjectMapper,
) {
    private val restClient: RestClient = RestClient.create()

    @Retryable(
        retryFor = [OdsayServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchPublicTransportRoute(startX: Double, startY: Double, endX: Double, endY: Double): OdsayRouteApiResponse {
        val url = String.format(
            "%s?apiKey=%s&SX=%s&SY=%s&EX=%s&EY=%s",
            odsayApiConfig.baseUrl,
            odsayApiConfig.apiKey,
            startX, startY, endX, endY
        )

        try {
            val uri = URI.create(url)

            val rawBody = restClient.get()
                .uri(uri)
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
