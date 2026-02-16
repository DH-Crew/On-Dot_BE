package com.dh.ondot.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class OpenApiConfig(
    private val environment: Environment,
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "AccessToken"

        val activeProfile = environment.getProperty("spring.profiles.active", "local")

        val serverUrl = System.getenv("PROD_SERVER_URL")
            ?.takeIf { it.isNotBlank() }
            ?: "http://localhost:8080"

        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .servers(listOf(Server().url(serverUrl).description("$activeProfile server")))
            .info(
                Info()
                    .title("On-Dot API Documentation")
                    .description("Team DH's On-Dot service API specification.")
                    .version("1.0"),
            )
    }
}
