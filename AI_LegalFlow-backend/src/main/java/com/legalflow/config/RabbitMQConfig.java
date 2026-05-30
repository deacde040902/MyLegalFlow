package com.legalflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String AGENT_EXCHANGE = "agent.exchange";
    public static final String TASK_QUEUE = "task.queue";
    public static final String RESULT_QUEUE = "result.queue";
    public static final String TASK_ROUTING_KEY = "task.execute";
    public static final String RESULT_ROUTING_KEY = "task.result";

    @Bean
    public TopicExchange agentExchange() {
        return new TopicExchange(AGENT_EXCHANGE);
    }

    @Bean
    public Queue taskQueue() {
        return new Queue(TASK_QUEUE, true);
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(RESULT_QUEUE, true);
    }

    @Bean
    public Binding taskBinding(Queue taskQueue, TopicExchange agentExchange) {
        return BindingBuilder.bind(taskQueue).to(agentExchange).with(TASK_ROUTING_KEY);
    }

    @Bean
    public Binding resultBinding(Queue resultQueue, TopicExchange agentExchange) {
        return BindingBuilder.bind(resultQueue).to(agentExchange).with(RESULT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
