package com.zxyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxyy.entity.UrlMap;
import org.apache.ibatis.annotations.Mapper;

/**
 * URL 映射表的基础数据访问接口。
 */
@Mapper
public interface UrlMapMapper extends BaseMapper<UrlMap> {
}
