package com.dh.ondot.core.config

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.P6SpyOptions
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import jakarta.annotation.PostConstruct
import org.hibernate.engine.jdbc.internal.FormatStyle
import org.springframework.context.annotation.Configuration
import java.util.Locale

@Configuration
class P6SpyFormatter : MessageFormattingStrategy {

    @PostConstruct
    fun setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().logMessageFormat = this::class.java.name
    }

    override fun formatMessage(
        connectionId: Int,
        now: String?,
        elapsed: Long,
        category: String?,
        prepared: String?,
        sql: String?,
        url: String?,
    ): String {
        val formattedSql = formatSql(category, sql)
        return "[$category] | $elapsed ms | $formattedSql"
    }

    private fun formatSql(category: String?, sql: String?): String {
        if (!sql.isNullOrBlank() && category == Category.STATEMENT.name) {
            val trimmedSQL = sql.trim().lowercase(Locale.ROOT)
            val formatter = when {
                trimmedSQL.startsWith("create") ||
                    trimmedSQL.startsWith("alter") ||
                    trimmedSQL.startsWith("comment") -> FormatStyle.DDL.formatter

                else -> FormatStyle.BASIC.formatter
            }
            return formatter.format(sql)
        }
        return sql ?: ""
    }
}
