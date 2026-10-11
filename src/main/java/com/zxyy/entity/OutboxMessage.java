package com.zxyy.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 本地消息：只删除已确认发送且超过保留期的数据，待发送消息不能清理。 */
@Data
@TableName("outbox_message")
public class OutboxMessage {
    public static final int PENDING = 0;
    public static final int SENT = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private String exchangeName;
    private String routingKey;
    private String payload;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String lastError;
    private LocalDateTime sentTime;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
