package com.example.simulator.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.queue.sensor-data}")
    private String sensorDataQueueName;

    @Bean
    public Queue sensorDataQueue() {
        return new Queue(sensorDataQueueName, true);
    }
}