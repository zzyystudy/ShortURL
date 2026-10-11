package com.zxyy.mq;

import com.zxyy.config.RabbitMqConfig;
import com.zxyy.entity.TransformEventRecord;
import com.zxyy.service.TransformEventRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** 入库正常结束后容器 ACK；异常交给重试和失败队列处理。 */
@Component
@RequiredArgsConstructor
public class TransformEventConsumer {

    private final TransformEventRecordService recordService;

    @RabbitListener(queues = RabbitMqConfig.TRANSFORM_EVENT_QUEUE)
    public void consume(TransformEventRecord event) {
        recordService.recordVisit(event);
    }
}
