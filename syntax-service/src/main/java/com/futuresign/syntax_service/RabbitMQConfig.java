package com.futuresign.syntax_service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${signing.exchange.name}")
    private String SIGNING_EXCHANGE_NAME;
    @Value("${syntax.check.routing.key}")
    private String SYNTAX_CHECK_ROUTING_KEY;
    @Value("${syntax.check.queue}")
    private String SYNTAX_CHECK_QUEUE;

    @Bean
    public DirectExchange signingExchange() {
        return new DirectExchange(SIGNING_EXCHANGE_NAME);
    }

    @Bean
    public Queue syntaxCheckQueue() {
        return new Queue(SYNTAX_CHECK_QUEUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Binding syntaxCheckBinding(Queue syntaxCheckQueue, DirectExchange documentExchange) {
        return BindingBuilder.bind(syntaxCheckQueue).to(documentExchange).with(SYNTAX_CHECK_ROUTING_KEY);
    }
}
