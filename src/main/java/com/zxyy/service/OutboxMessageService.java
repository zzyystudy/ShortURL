package com.zxyy.service;

import com.zxyy.entity.TransformEventRecord;

public interface OutboxMessageService {
    /** 在本地事务中保存完整事件；返回时事务已提交（若参与外层事务，则随外层提交）。 */
    void saveVisitEvent(TransformEventRecord event);
}
