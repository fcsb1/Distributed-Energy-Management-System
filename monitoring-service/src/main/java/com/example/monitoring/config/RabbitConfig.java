package com.example.monitoring.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public static final String EXCHANGE_NAME = "system.exchange";

    // 1. Define the Topic Exchange (Required in ALL services)
    @Bean
    public TopicExchange systemExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Value("${app.queue.sensor-data}")
    private String sensorDataQueueName;

    @Bean
    public Queue sensorDataQueue() {
        // durable = true means queue survives broker restart
        return new Queue(sensorDataQueueName, true);
    }



    @Bean
    public Queue monitoringDeviceSyncQueue() {
        return new Queue("monitoring.device.sync.queue", true);
    }

    @Bean
    public Binding monitoringDeviceBinding(Queue monitoringDeviceSyncQueue, TopicExchange systemExchange) {
        // Monitoring listens to device events
        return BindingBuilder.bind(monitoringDeviceSyncQueue).to(systemExchange).with("device.event.#");
    }

}