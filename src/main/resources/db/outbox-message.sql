-- 在 shortURL 数据库中执行一次；应用不会自动建表。没有 version 字段。
CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '本地消息主键',
    event_id BIGINT NOT NULL COMMENT '访问事件ID，重投保持不变',
    exchange_name VARCHAR(128) NOT NULL COMMENT '目标交换机',
    routing_key VARCHAR(128) NOT NULL COMMENT '路由键',
    payload JSON NOT NULL COMMENT '完整访问事件JSON',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待发送，1已确认发送',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '发送失败次数',
    next_retry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次允许重试时间',
    last_error VARCHAR(1000) DEFAULT NULL COMMENT '最近一次发送错误',
    sent_time DATETIME DEFAULT NULL COMMENT '收到确认并更新成功的时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_event_id (event_id),
    KEY idx_outbox_ready (status, next_retry_time, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RabbitMQ本地待发送消息';
