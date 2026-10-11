package com.zxyy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecovererWithConfirms;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 的 JSON 消息转换和短链访问事件队列配置。
 */
@Configuration(proxyBeanMethods = false)
public class RabbitMqConfig {

    public static final String TRANSFORM_EVENT_EXCHANGE = "shorturl.transform.exchange";
    public static final String TRANSFORM_EVENT_QUEUE = "shorturl.transform.queue";
    public static final String TRANSFORM_EVENT_ROUTING_KEY = "shorturl.transform";
    public static final String TRANSFORM_ERROR_EXCHANGE = "shorturl.transform.error.exchange";
    public static final String TRANSFORM_ERROR_QUEUE = "shorturl.transform.error.queue";
    public static final String TRANSFORM_ERROR_ROUTING_KEY = "shorturl.transform.error";

    @Bean
    public RabbitTemplateCustomizer mandatoryRouting() {
        return template -> template.setMandatory(true);
    }

    @Bean
    public DirectExchange transformErrorExchange() {
        return new DirectExchange(TRANSFORM_ERROR_EXCHANGE, true, false);
    }

    @Bean
    public Queue transformErrorQueue() {
        return QueueBuilder.durable(TRANSFORM_ERROR_QUEUE).build();
    }

    @Bean
    public Binding transformErrorBinding(@Qualifier("transformErrorQueue") Queue queue,
                                         @Qualifier("transformErrorExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TRANSFORM_ERROR_ROUTING_KEY);
    }

    /** 消费重试耗尽后转存失败队列，等待 MQ 确认转存结果。 */
    @Bean
    public MessageRecoverer visitMessageRecoverer(RabbitTemplate rabbitTemplate) {
        RepublishMessageRecovererWithConfirms recoverer = new RepublishMessageRecovererWithConfirms(
                rabbitTemplate, TRANSFORM_ERROR_EXCHANGE, TRANSFORM_ERROR_ROUTING_KEY, ConfirmType.CORRELATED);
        recoverer.setConfirmTimeout(5000);
        return recoverer;
    }

    /**
     * Spring Boot 会将此转换器用于 RabbitTemplate 和默认的消息监听容器。
     * 使用 Boot 管理的 ObjectMapper，支持 LocalDate、LocalDateTime 等字段。
     */
    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public DirectExchange transformEventExchange() {
        return new DirectExchange(TRANSFORM_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue transformEventQueue() {
        return QueueBuilder.durable(TRANSFORM_EVENT_QUEUE).build();
    }

    /** 路由键完全匹配时，将消息投递到访问事件队列。 */
    @Bean
    public Binding transformEventBinding(@Qualifier("transformEventQueue") Queue transformEventQueue,
                                         @Qualifier("transformEventExchange") DirectExchange transformEventExchange) {
        return BindingBuilder.bind(transformEventQueue)
                .to(transformEventExchange)
                .with(TRANSFORM_EVENT_ROUTING_KEY);
    }
}
