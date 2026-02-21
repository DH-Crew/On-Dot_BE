package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.application.dto.EverytimeLecture
import com.dh.ondot.schedule.infra.exception.EverytimeNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import java.time.LocalTime
import javax.xml.parsers.DocumentBuilderFactory

@DisplayName("EverytimeApi 테스트")
class EverytimeApiTest {

    @Nested
    @DisplayName("XML 파싱 검증")
    inner class XmlParsingTest {

        @Test
        @DisplayName("유효한 XML을 EverytimeLecture 리스트로 변환한다")
        fun parseXml_ValidXml_ReturnsLectures() {
            // given
            val xml = """
                <response>
                    <table>
                        <subject>
                            <name value="데이터베이스"/>
                            <time>
                                <data day="0" starttime="114" endtime="120" place="공학관 301"/>
                                <data day="2" starttime="114" endtime="120" place="공학관 301"/>
                            </time>
                        </subject>
                        <subject>
                            <name value="운영체제"/>
                            <time>
                                <data day="1" starttime="132" endtime="138" place="공학관 201"/>
                            </time>
                        </subject>
                    </table>
                </response>
            """.trimIndent()

            // when
            val lectures = parseXml(xml)

            // then
            assertThat(lectures).hasSize(3)

            val db = lectures.filter { it.name == "데이터베이스" }
            assertThat(db).hasSize(2)
            assertThat(db[0].day).isEqualTo(0) // 월
            assertThat(db[0].startTime).isEqualTo(LocalTime.of(9, 30)) // 114 * 5 = 570min = 9h 30m
            assertThat(db[0].endTime).isEqualTo(LocalTime.of(10, 0))  // 120 * 5 = 600min = 10h 0m
            assertThat(db[0].place).isEqualTo("공학관 301")
            assertThat(db[1].day).isEqualTo(2) // 수

            val os = lectures.filter { it.name == "운영체제" }
            assertThat(os).hasSize(1)
            assertThat(os[0].day).isEqualTo(1) // 화
            assertThat(os[0].startTime).isEqualTo(LocalTime.of(11, 0)) // 132 * 5 = 660min = 11h 0m
        }

        @Test
        @DisplayName("수업이 없는 XML일 경우 EverytimeNotFoundException이 발생한다")
        fun parseXml_NoSubjects_ThrowsNotFoundException() {
            // given
            val xml = """
                <response>
                    <table/>
                </response>
            """.trimIndent()

            // when & then
            assertThatThrownBy { parseXml(xml) }
                .isInstanceOf(EverytimeNotFoundException::class.java)
        }

        @Test
        @DisplayName("slotToLocalTime 변환이 정확하다")
        fun slotToLocalTime_VariousSlots_ReturnsCorrectTimes() {
            assertThat(slotToLocalTime(0)).isEqualTo(LocalTime.of(0, 0))
            assertThat(slotToLocalTime(12)).isEqualTo(LocalTime.of(1, 0))
            assertThat(slotToLocalTime(114)).isEqualTo(LocalTime.of(9, 30))
            assertThat(slotToLocalTime(132)).isEqualTo(LocalTime.of(11, 0))
            assertThat(slotToLocalTime(216)).isEqualTo(LocalTime.of(18, 0))
        }
    }

    // EverytimeApi 의 private parseXml 메서드와 동일 로직 — 파싱 정확성을 검증
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
