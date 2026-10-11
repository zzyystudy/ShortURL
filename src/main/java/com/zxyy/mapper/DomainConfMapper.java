package com.zxyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxyy.entity.DomainConf;
import org.apache.ibatis.annotations.Mapper;

/**
 * 域名配置表的基础数据访问接口。
 */
@Mapper
public interface DomainConfMapper extends BaseMapper<DomainConf> {
}
