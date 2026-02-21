package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.application.dto.EverytimeLecture
import com.dh.ondot.schedule.infra.exception.EverytimeNotFoundException
import com.dh.ondot.schedule.infra.exception.EverytimeServerException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.w3c.dom.Element
import java.time.LocalTime
import javax.xml.parsers.DocumentBuilderFactory

@Component
class EverytimeApi(
    @Qualifier("everytimeRestClient") private val everytimeRestClient: RestClient,
) {
    @Retryable(
        retryFor = [EverytimeServerException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500),
    )
    fun fetchTimetable(identifier: String): List<EverytimeLecture> {
        val xml = callApi(identifier)
        return parseXml(xml)
    }

    private fun callApi(identifier: String): String {
        try {
            val response = everytimeRestClient.post()
                .uri("/find/timetable/table/friend")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("identifier=$identifier&friendInfo=true")
                .retrieve()
                .body(String::class.java)

            if (response.isNullOrBlank()) {
                throw EverytimeNotFoundException()
            }
            return response
        } catch (ex: EverytimeNotFoundException) {
            throw ex
        } catch (ex: RestClientResponseException) {
            throw EverytimeServerException("${ex.statusCode}: ${ex.message}")
        } catch (ex: Exception) {
            throw EverytimeServerException("${ex.javaClass.simpleName}: ${ex.message}")
        }
    }

    private fun parseXml(xml: String): List<EverytimeLecture> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(xml.byteInputStream())
        val subjects = document.getElementsByTagName("subject")

        if (subjects.length == 0) {
            throw EverytimeNotFoundException()
        }

        val lectures = mutableListOf<EverytimeLecture>()

        for (i in 0 until subjects.length) {
            val subject = subjects.item(i) as Element
            val name = subject.getElementsByTagName("name").item(0)
                ?.let { (it as Element).getAttribute("value") } ?: continue

            val timeElements = subject.getElementsByTagName("data")
            for (j in 0 until timeElements.length) {
                val data = timeElements.item(j) as Element
                val day = data.getAttribute("day").toIntOrNull() ?: continue
                val startSlot = data.getAttribute("starttime").toIntOrNull() ?: continue
                val endSlot = data.getAttribute("endtime").toIntOrNull() ?: continue
                val place = data.getAttribute("place") ?: ""

                lectures.add(
                    EverytimeLecture(
                        name = name,
                        day = day,
                        startTime = slotToLocalTime(startSlot),
                        endTime = slotToLocalTime(endSlot),
                        place = place,
                    )
                )
            }
        }

        return lectures
    }

    private fun slotToLocalTime(slot: Int): LocalTime {
        val totalMinutes = slot * 5
        return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
    }
}
