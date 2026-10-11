package com.zxyy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxyy.entity.VisitStatistics;
import com.zxyy.mapper.VisitStatisticsMapper;
import com.zxyy.service.VisitStatisticsService;
import org.springframework.stereotype.Service;

/**
 * 每日访问统计服务实现，继承 MyBatis-Plus 的基础 CRUD 能力。
 */
@Service
public class VisitStatisticsServiceImpl extends ServiceImpl<VisitStatisticsMapper, VisitStatistics>
        implements VisitStatisticsService {
}
