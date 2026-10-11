package com.zxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxyy.entity.TransformEventRecord;

/**
 * 访问明细的基础增删改查服务。
 */
public interface TransformEventRecordService extends IService<TransformEventRecord> {

    /** 解析访客信息并保存，同一事件 ID 不重复入库。 */
    void recordVisit(TransformEventRecord event);
}
