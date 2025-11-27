package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Bean
    public TopicExchange systemExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue authUserSyncQueue() {
        return new Queue("auth.user.sync.queue", true);
    }

    @Bean
    public Binding authBinding(Queue authUserSyncQueue, TopicExchange systemExchange) {
        // Listen to all user events
        return BindingBuilder.bind(authUserSyncQueue).to(systemExchange).with("user.event.#");
    }

}