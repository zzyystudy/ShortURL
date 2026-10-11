package com.zxyy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxyy.entity.DomainConf;
import com.zxyy.mapper.DomainConfMapper;
import com.zxyy.service.DomainConfService;
import org.springframework.stereotype.Service;

/**
 * 域名配置服务实现，继承 MyBatis-Plus 的基础 CRUD 能力。
 */
@Service
public class DomainConfServiceImpl extends ServiceImpl<DomainConfMapper, DomainConf>
        implements DomainConfService {
}
