package com.zxyy.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxyy.entity.TransformEventRecord;
import com.zxyy.mapper.TransformEventRecordMapper;
import com.zxyy.service.TransformEventRecordService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 访问明细入库：补充访客标识和 UA 信息，用事件主键实现幂等消费。
 */
@Service
public class TransformEventRecordServiceImpl
        extends ServiceImpl<TransformEventRecordMapper, TransformEventRecord>
        implements TransformEventRecordService {

    private static final Pattern ANDROID_MODEL = Pattern.compile(
            "Android[^;)]*;\\s*(?:[a-z]{2}[-_][A-Za-z]{2};\\s*)?([^;)]+?)(?:\\s+Build/[^;)]+)?\\)");
    private final Validator validator;

    public TransformEventRecordServiceImpl(TransformEventRecordMapper mapper, Validator validator) {
        this.baseMapper = mapper;
        this.validator = validator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordVisit(TransformEventRecord event) {
        if (event == null || event.getId() == null || event.getId() <= 0) {
            throw new IllegalArgumentException("访问事件必须携带有效的事件ID");
        }
        event.setUniqueIdentity(DigestUtil.sha1Hex(event.getClientIp() + "-"
                + StrUtil.nullToEmpty(event.getUserAgent())));
        event.setShortUrlDigest(DigestUtil.sha1Hex(event.getShortUrl()));
        event.setLongUrlDigest(DigestUtil.sha1Hex(event.getLongUrl()));
        event.setDeleted(0);
        if (StrUtil.isNotBlank(event.getUserAgent())) {
            UserAgent agent = UserAgentUtil.parse(event.getUserAgent());
            event.setBrowserType(StrUtil.subPre(agent.getBrowser().getName(), 64));
            event.setBrowserVersion(StrUtil.subPre(agent.getVersion(), 128));
            event.setOsType(StrUtil.subPre(agent.getOs().getName(), 32));
            event.setOsVersion(StrUtil.subPre(agent.getOsVersion(), 32));
            event.setDeviceType(StrUtil.subPre(agent.getPlatform().getName(), 32));
            event.setPhoneType(phoneModel(event.getUserAgent()));
        }
        // 省份和城市需要 IP 地理库，此阶段保持空值。
        var violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        try {
            if (baseMapper.insert(event) != 1) {
                throw new IllegalStateException("访问事件保存失败");
            }
        } catch (DuplicateKeyException exception) {
            // 数据库已提交但 ACK 丢失时，重投只会带来同一个事件主键。
            if (baseMapper.selectById(event.getId()) == null) {
                throw exception;
            }
        }
    }

    private String phoneModel(String userAgent) {
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }
        if (userAgent.contains("iPad")) {
            return "iPad";
        }
        Matcher matcher = ANDROID_MODEL.matcher(userAgent);
        return matcher.find() ? StrUtil.subPre(matcher.group(1).trim(), 64) : null;
    }
}
