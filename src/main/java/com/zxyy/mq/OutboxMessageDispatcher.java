package com.zxyy.mq;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxyy.entity.OutboxMessage;
import com.zxyy.entity.TransformEventRecord;
import com.zxyy.mapper.OutboxMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** 单体串行扫描；网络等待期间不持有数据库事务。崩溃时仍是待发送状态，重启后继续发。 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "short-url.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxMessageDispatcher {
    private final OutboxMessageMapper mapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;
    private final long confirmTimeoutMs;

    public OutboxMessageDispatcher(OutboxMessageMapper mapper, RabbitTemplate rabbitTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${short-url.outbox.batch-size:20}") int batchSize,
                                  @Value("${short-url.outbox.confirm-timeout-ms:5000}") long confirmTimeoutMs) {
        if (batchSize <= 0 || confirmTimeoutMs <= 0) {
            throw new IllegalArgumentException("本地消息批量大小和确认超时必须大于0");
        }
        this.mapper = mapper;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.confirmTimeoutMs = confirmTimeoutMs;
    }

    @Scheduled(fixedDelayString = "${short-url.outbox.fixed-delay-ms:2000}",
            initialDelayString = "${short-url.outbox.initial-delay-ms:5000}")
    public synchronized void dispatchBatch() {
        // fixedDelay 在上一轮结束后计时；synchronized 同时防止本进程内手工触发与任务重叠。
        for (OutboxMessage message : mapper.findReady(batchSize)) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            dispatch(message);
        }
    }

    private void dispatch(OutboxMessage message) {
        try {
            TransformEventRecord event = objectMapper.readValue(message.getPayload(), TransformEventRecord.class);
            if (!message.getEventId().equals(event.getId())) {
                throw new IllegalStateException("本地消息事件ID与内容不一致");
            }
            // 事件ID始终不变；每次发送的 correlation ID 不同，避免超时后迟到的确认干扰下一次发送。
            CorrelationData correlation = new CorrelationData(message.getEventId() + ":" + UUID.randomUUID());
            rabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), event, outgoing -> {
                outgoing.getMessageProperties().setMessageId(message.getEventId().toString());
                outgoing.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return outgoing;
            }, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (correlation.getReturned() != null) {
                throw new IllegalStateException("消息无法路由：" + correlation.getReturned().getReplyText());
            }
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ拒绝确认：" + confirm.getReason());
            }
            // ACK 只表示 MQ 接收且未退回，不表示消费者已入库。更新失败仍会再次投递。
            mapper.markSent(message.getId());
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            long delaySeconds = Math.min(300L, 1L << Math.min(message.getRetryCount(), 9));
            String error = StrUtil.subPre(exception.getClass().getSimpleName() + ": " + exception.getMessage(), 1000);
            log.warn("本地消息发送失败，事件ID：{}，{}秒后重试：{}", message.getEventId(), delaySeconds, error);
            try {
                // 不设放弃次数；延迟从1秒递增，最多5分钟。失败记录保留以便排查。
                mapper.postpone(message.getId(), delaySeconds, error);
            } catch (RuntimeException updateException) {
                // 连重试时间都更新失败时，原行仍为待发送，下次扫描或重启会再次尝试。
                log.error("本地消息重试状态保存失败，事件ID：{}", message.getEventId(), updateException);
            }
        }
    }
}
