package com.dh.ondot.member.application.command

data class CreateAddressCommand(
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
)
