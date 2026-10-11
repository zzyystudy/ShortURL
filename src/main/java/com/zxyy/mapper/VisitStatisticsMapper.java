package com.zxyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxyy.entity.VisitStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日访问统计表的基础数据访问接口。
 */
@Mapper
public interface VisitStatisticsMapper extends BaseMapper<VisitStatistics> {
}
