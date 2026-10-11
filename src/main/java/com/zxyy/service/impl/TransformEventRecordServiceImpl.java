package com.zxyy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxyy.entity.TransformEventRecord;
import com.zxyy.mapper.TransformEventRecordMapper;
import com.zxyy.service.TransformEventRecordService;
import org.springframework.stereotype.Service;

/**
 * 访问明细服务实现，继承 MyBatis-Plus 的基础 CRUD 能力。
 */
@Service
public class TransformEventRecordServiceImpl
        extends ServiceImpl<TransformEventRecordMapper, TransformEventRecord>
        implements TransformEventRecordService {
}
