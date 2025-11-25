package com.example.demo.config; // Check package name per service

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "system.exchange";

    // 1. Define the Topic Exchange (Required in ALL services)
    @Bean
    public TopicExchange systemExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Note: The Queues and Bindings will be specific to the CONSUMER services
    // (Auth, Device, Monitoring) in the steps below.
}