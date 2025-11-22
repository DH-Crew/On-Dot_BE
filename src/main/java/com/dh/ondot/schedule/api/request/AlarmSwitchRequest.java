package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.NotNull;

public record AlarmSwitchRequest(
        @NotNull Boolean isEnabled
) {
}
