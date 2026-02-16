package com.dh.ondot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class OnDotApplication

fun main(args: Array<String>) {
    runApplication<OnDotApplication>(*args)
}
