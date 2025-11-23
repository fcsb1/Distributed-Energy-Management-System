package com.example.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.queue.device-sync}")
    private String deviceSyncQueueName;

    @Bean
    public Queue deviceSyncQueue() {
        // durable = true means queue survives broker restart
        return new Queue(deviceSyncQueueName, true);
    }
}