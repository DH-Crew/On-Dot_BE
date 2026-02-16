package com.dh.ondot.schedule.application.command

data class SavePlaceHistoryCommand(
    val title: String,
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
)
