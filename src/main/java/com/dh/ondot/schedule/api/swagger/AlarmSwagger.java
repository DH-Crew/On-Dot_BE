package com.dh.ondot.schedule.api.swagger;

import com.dh.ondot.schedule.api.response.LatestAlarmResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/*──────────────────────────────────────────────────────────────
 * Alarm Swagger
 *──────────────────────────────────────────────────────────────*/
@Tag(
        name = "Alarm API",
        description = """
        <b>AccessToken (Authorization: Bearer JWT)</b>은 필수입니다.<br>
        <br>
        <b>🔔 Alarm ENUM</b><br>
        • <code>AlarmMode</code>: <code>SILENT</code>, <code>VIBRATE</code>, <code>SOUND</code><br>
        • <code>SnoozeInterval</code>: 1, 3, 5, 10, 30, 60 (분)<br>
        • <code>SnoozeCount</code>: -1(INFINITE), 1, 3, 5, 10 (회)<br>
        • <code>SoundCategory</code>: <i>BIRD, OCEAN, DEFAULT …</i><br>
        • <code>RingTone</code>: <i>beep.mp3, morning.mp3 …</i>
        """
)
@RequestMapping("/alarms")
public interface AlarmSwagger {

    /*──────────────────────────────────────────────────────
     * 1. 최신 알람 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "가장 최근 수정된 스케줄의 알람 설정 조회",
            description = """
            사용자의 스케줄 중 <code>updatedAt</code>이 가장 최신인 1건을 기준으로  
            <b>준비 알람</b>과 <b>출발 알람</b> 설정 값을 반환합니다.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    schema  = @Schema(implementation = LatestAlarmResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "preparationAlarm": {
                            "alarmMode": "VIBRATE",
                            "isEnabled": true,
                            "triggeredAt": "2025-05-10T18:30:00",
                            "isSnoozeEnabled": true,
                            "snoozeInterval": 5,
                            "snoozeCount": 3,
                            "soundCategory": "BIRD",
                            "ringTone": "morning.mp3",
                            "volume": 7
                          },
                          "departureAlarm": {
                            "alarmMode": "SOUND",
                            "isEnabled": true,
                            "triggeredAt": "2025-05-10T18:50:00",
                            "isSnoozeEnabled": false,
                            "snoozeInterval": 0,
                            "snoozeCount": -1,
                            "soundCategory": "DEFAULT",
                            "ringTone": "beep.mp3",
                            "volume": 8
                          }
                        }"""
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "스케줄 없음")
            }
    )
    @GetMapping("/latest")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    LatestAlarmResponse getLatestAlarms(
            @RequestAttribute("memberId") Long memberId
    );
}
