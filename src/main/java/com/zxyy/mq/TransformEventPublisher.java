package com.zxyy.mq;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zxyy.entity.TransformEventRecord;
import com.zxyy.entity.UrlMap;
import com.zxyy.service.OutboxMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** 采集请求信息并提交本地消息；RabbitMQ 发送由定时任务负责。 */
@Component
@RequiredArgsConstructor
public class TransformEventPublisher {

    private final OutboxMessageService outboxMessageService;

    public void publish(UrlMap mapping, HttpServletRequest request) {
        TransformEventRecord event = new TransformEventRecord();
        // 单体固定节点的雪花 ID 作为事件主键，与短码的 MurmurHash 无关。
        // 重投沿用此 ID，使用现有主键去重，不增加表字段。
        event.setId(IdUtil.getSnowflake(1, 1).nextId());
        event.setCompressionCode(mapping.getCompressionCode());
        event.setShortUrl(mapping.getShortUrl());
        event.setLongUrl(mapping.getLongUrl());
        event.setShortUrlDigest(mapping.getShortUrlDigest());
        event.setLongUrlDigest(mapping.getLongUrlDigest());
        // 未配置可信代理时，不直接相信可伪造的 X-Forwarded-For。
        event.setClientIp(StrUtil.blankToDefault(request.getRemoteAddr(), "unknown"));
        event.setUserAgent(StrUtil.subPre(request.getHeader("User-Agent"), 2048));
        event.setCookieValue(StrUtil.subPre(request.getHeader("Cookie"), 2048));
        event.setQueryParam(StrUtil.subPre(request.getQueryString(), 2048));
        event.setRecordTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        // 状态 3 表示已准备返回 302，不代表目标页面加载成功。
        event.setTransformStatus(3);
        // Spring 事务代理在方法返回前提交。失败向上抛出，不能返回成功却漏掉访问事件。
        outboxMessageService.saveVisitEvent(event);
    }
}
