package com.dh.ondot.core.config

import com.dh.ondot.core.TokenInterceptor
import com.dh.ondot.member.core.OauthProviderConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val oauthProviderConverter: OauthProviderConverter,
    private val tokenInterceptor: TokenInterceptor,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(oauthProviderConverter)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tokenInterceptor)
            .addPathPatterns("/members/**", "/alarms/**", "/places/**", "/schedules/**")
            .excludePathPatterns(
                "/schedules/*/issues",
                "/schedules/*/preparation",
            )
    }
}
