package com.zxyy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxyy.entity.UrlMap;
import com.zxyy.mapper.UrlMapMapper;
import com.zxyy.service.UrlMapService;
import org.springframework.stereotype.Service;

/**
 * URL 映射服务实现，继承 MyBatis-Plus 的基础 CRUD 能力。
 */
@Service
public class UrlMapServiceImpl extends ServiceImpl<UrlMapMapper, UrlMap>
        implements UrlMapService {
}
