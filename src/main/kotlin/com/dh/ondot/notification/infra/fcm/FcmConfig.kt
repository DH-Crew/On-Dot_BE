package com.dh.ondot.notification.infra.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
class FcmConfig(
    @Value("\${fcm.service-account-file:}")
    private val serviceAccountFile: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isNotEmpty()) return
        if (serviceAccountFile.isBlank()) {
            log.warn("FCM service account file not configured. Push notifications will not work.")
            return
        }
        try {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(FileInputStream(serviceAccountFile)))
                .build()
            FirebaseApp.initializeApp(options)
        } catch (e: Exception) {
            log.error("Failed to initialize FirebaseApp: {}", e.message, e)
        }
    }
}
