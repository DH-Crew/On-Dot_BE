package com.dh.ondot.member.application.command

data class UpdateHomeAddressCommand(
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
)
