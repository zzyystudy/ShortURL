package com.zxyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxyy.entity.TransformEventRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问明细表的基础数据访问接口。
 */
@Mapper
public interface TransformEventRecordMapper extends BaseMapper<TransformEventRecord> {
}
