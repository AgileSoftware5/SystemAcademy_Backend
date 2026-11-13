package io.github.agilesoftware5.systemacademy.shared.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Pool acotado para llamadas al LLM: pocos hilos para no agotar la cuota del proveedor.
     * Con la cola llena se descarta la tarea (las respuestas quedan PENDING para reintento manual)
     * en vez de bloquear o hacer fallar la entrega del alumno.
     */
    @Bean(name = "gradingExecutor")
    public Executor gradingExecutor(
            @Value("${grading.async.pool-size:2}") int poolSize,
            @Value("${grading.async.queue-capacity:500}") int queueCapacity
    ) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("grading-");
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler((task, pool) ->
                log.warn("cola de calificación llena (capacidad={}): tarea descartada, queda para reintento manual",
                        queueCapacity));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
