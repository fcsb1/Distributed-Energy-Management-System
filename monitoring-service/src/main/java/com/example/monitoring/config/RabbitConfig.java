package com.example.monitoring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.queue.sensor-data}")
    private String sensorDataQueueName;

    @Value("${app.queue.device-sync}")
    private String deviceSyncQueueName;

    @Bean
    public Queue sensorDataQueue() {
        // durable = true means queue survives broker restart
        return new Queue(sensorDataQueueName, true);
    }

    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(deviceSyncQueueName, true);
    }
}