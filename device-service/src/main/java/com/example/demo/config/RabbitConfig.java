package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public static final String EXCHANGE_NAME = "system.exchange";

    // Topic Exchange (Required in ALL services)
    @Bean
    public TopicExchange systemExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue deviceUserSyncQueue() {
        return new Queue("device.user.sync.queue", true);
    }

    @Bean
    public Binding deviceUserBinding(Queue deviceUserSyncQueue, TopicExchange systemExchange) {
        // Device service listens to user events
        return BindingBuilder.bind(deviceUserSyncQueue).to(systemExchange).with("user.event.#");
    }


}