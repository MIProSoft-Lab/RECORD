package es.uib.record.backend.journals.infrastructure.config

import java.util.concurrent.Executor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
class AsyncConfig {

    /**
     * Executor dedicado de un solo hilo para el volcado de journals. Al ser de un único hilo
     * garantiza que solo se ejecute una sincronización a la vez, sin bloquear el hilo del cron.
     */
    @Bean("journalSyncExecutor")
    fun journalSyncExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 1
            maxPoolSize = 1
            queueCapacity = 1
            setThreadNamePrefix("journal-sync-")
            initialize()
        }
    }
}
