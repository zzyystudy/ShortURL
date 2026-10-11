package com.zxyy.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxyy.config.RabbitMqConfig;
import com.zxyy.entity.OutboxMessage;
import com.zxyy.entity.TransformEventRecord;
import com.zxyy.mapper.OutboxMessageMapper;
import com.zxyy.service.OutboxMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxMessageServiceImpl implements OutboxMessageService {
    private final OutboxMessageMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveVisitEvent(TransformEventRecord event) {
        if (event == null || event.getId() == null || event.getId() <= 0) {
            throw new IllegalArgumentException("访问事件必须携带有效的事件ID");
        }
        OutboxMessage message = new OutboxMessage();
        message.setEventId(event.getId());
        message.setExchangeName(RabbitMqConfig.TRANSFORM_EVENT_EXCHANGE);
        message.setRoutingKey(RabbitMqConfig.TRANSFORM_EVENT_ROUTING_KEY);
        message.setStatus(OutboxMessage.PENDING);
        message.setRetryCount(0);
        // next_retry_time 使用数据库默认时间，避免应用与数据库时钟偏差。
        try {
            message.setPayload(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("访问事件序列化失败", exception);
        }
        if (mapper.insert(message) != 1) {
            throw new IllegalStateException("访问事件本地保存失败");
        }
    }
}
