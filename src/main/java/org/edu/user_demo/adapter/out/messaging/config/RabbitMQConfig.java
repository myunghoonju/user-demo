package org.edu.user_demo.adapter.out.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SIGNUP_QUEUE = "signup.queue";
    public static final String SIGNUP_DLQ = "signup.dlq";
    public static final String SIGNUP_EXCHANGE = "signup.exchange";
    public static final String SIGNUP_DLX = "signup.dlx";

    @Bean
    public Queue signupQueue() {
        return QueueBuilder.durable(SIGNUP_QUEUE)
                           .quorum()
                           .withArgument("x-dead-letter-exchange", SIGNUP_DLX)
                           .withArgument("x-dead-letter-routing-key", SIGNUP_DLQ)
                           .build();
    }

    @Bean
    public Queue signupDlq() {
        return QueueBuilder.durable(SIGNUP_DLQ)
                           .quorum()
                           .build();
    }

    @Bean
    public DirectExchange signupExchange() {
        return new DirectExchange(SIGNUP_EXCHANGE);
    }

    @Bean
    public DirectExchange signupDlx() {
        return new DirectExchange(SIGNUP_DLX);
    }

    @Bean
    public Binding signupBinding() {
        return BindingBuilder.bind(signupQueue())
                             .to(signupExchange())
                             .with(SIGNUP_QUEUE);
    }

    @Bean
    public Binding signupDlqBinding() {
        return BindingBuilder.bind(signupDlq())
                             .to(signupDlx())
                             .with(SIGNUP_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory dlqListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(2);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
