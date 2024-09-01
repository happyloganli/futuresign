package com.future_sign.document_service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue signingQueue() {
        return new Queue("signing-queue", true);
    }

    @Bean
    public TopicExchange signingExchange() {
        return new TopicExchange("signing-exchange");
    }

    @Bean
    public Binding signingBinding(Queue signingQueue, TopicExchange signingExchange) {
        return BindingBuilder.bind(signingQueue).to(signingExchange).with("signing.#");
    }
}
