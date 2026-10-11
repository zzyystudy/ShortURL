package com.zxyy.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxyy.dto.CreateShortUrlRequest;
import com.zxyy.entity.DomainConf;
import com.zxyy.entity.UrlMap;
import com.zxyy.exception.BusinessException;
import com.zxyy.exception.ShortUrlNotFoundException;
import com.zxyy.mapper.DomainConfMapper;
import com.zxyy.mapper.UrlMapMapper;
import com.zxyy.service.UrlMapService;
import com.zxyy.utils.RedisCache;
import com.zxyy.utils.ShortCodeGenerator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 创建和跳转的核心业务。MySQL 是正式数据源，Redis 不可用时回退到数据库。
 */
@Slf4j
@Service
public class UrlMapServiceImpl extends ServiceImpl<UrlMapMapper, UrlMap>
        implements UrlMapService {

    private static final String CACHE_PREFIX = "shorturl:url:";
    private static final int MAX_GENERATE_ATTEMPTS = 10;
    private static final int CACHE_TTL_SECONDS = 1800;
    private static final int MISSING_TTL_SECONDS = 60;

    private final DomainConfMapper domainConfMapper;
    private final RedisCache redisCache;
    private final ShortCodeGenerator shortCodeGenerator;
    private final Validator validator;
    private final String defaultDomain;
    private final TransactionTemplate transactionTemplate;

    public UrlMapServiceImpl(UrlMapMapper urlMapMapper, DomainConfMapper domainConfMapper,
                             RedisCache redisCache, ShortCodeGenerator shortCodeGenerator,
                             Validator validator, PlatformTransactionManager transactionManager,
                             @Value("${short-url.default-domain:localhost:8778}") String defaultDomain) {
        this.baseMapper = urlMapMapper;
        this.domainConfMapper = domainConfMapper;
        this.redisCache = redisCache;
        this.shortCodeGenerator = shortCodeGenerator;
        this.validator = validator;
        this.defaultDomain = defaultDomain;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        // 每次候选码单独事务：冲突的插入回滚后再重试，成功提交后才更新缓存。
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public UrlMap createShortUrl(CreateShortUrlRequest request) {
        if (request == null) {
            throw new BusinessException("创建参数不能为空");
        }
        // Service 也校验参数，使内部 Java 调用与 HTTP 调用遵循相同规则。
        validate(request);
        DomainConf domain = domainConfMapper.selectOne(Wrappers.<DomainConf>lambdaQuery()
                .eq(DomainConf::getDomainValue, defaultDomain)
                .eq(DomainConf::getDomainStatus, 1));
        if (domain == null) {
            throw new BusinessException("短链域名未配置或已禁用：" + defaultDomain);
        }
        validateDomain(domain);

        for (int attempt = 0; attempt < MAX_GENERATE_ATTEMPTS; attempt++) {
            String code = shortCodeGenerator.generate(request.getLongUrl(), attempt);
            UrlMap urlMap = new UrlMap();
            urlMap.setCompressionCode(code);
            urlMap.setLongUrl(request.getLongUrl());
            urlMap.setShortUrl(domain.getProtocol() + "://" + domain.getDomainValue() + "/" + code);
            urlMap.setDescription(request.getDescription());
            urlMap.setShortUrlDigest(DigestUtil.sha1Hex(urlMap.getShortUrl()));
            urlMap.setLongUrlDigest(DigestUtil.sha1Hex(urlMap.getLongUrl()));
            urlMap.setUrlStatus(1);
            urlMap.setDeleted(0);
            validate(urlMap);

            try {
                transactionTemplate.executeWithoutResult(status -> {
                    if (baseMapper.insert(urlMap) != 1) {
                        throw new BusinessException("短链保存失败");
                    }
                });
            } catch (DuplicateKeyException exception) {
                // 不先查再插：并发请求仍可能冲突，以唯一索引的插入结果为准。
                continue;
            }
            cacheResult(code, urlMap, CACHE_TTL_SECONDS);
            return urlMap;
        }
        throw new BusinessException("短码生成冲突，请重试");
    }

    @Override
    public UrlMap resolveShortUrl(String compressionCode) {
        if (compressionCode == null || !compressionCode.matches("[0-9a-zA-Z]{6}")) {
            throw new ShortUrlNotFoundException();
        }
        Object cached = null;
        try {
            cached = redisCache.getCacheObject(CACHE_PREFIX + compressionCode);
        } catch (RuntimeException exception) {
            log.warn("读取短链缓存失败，回查数据库，短码：{}", compressionCode, exception);
        }
        if (cached instanceof UrlMap urlMap
                && Objects.equals(urlMap.getUrlStatus(), 1)
                && !Objects.equals(urlMap.getDeleted(), 1)) {
            return urlMap;
        }
        if (Boolean.FALSE.equals(cached)) {
            throw new ShortUrlNotFoundException();
        }

        UrlMap urlMap = baseMapper.selectOne(Wrappers.<UrlMap>lambdaQuery()
                .eq(UrlMap::getCompressionCode, compressionCode)
                .eq(UrlMap::getUrlStatus, 1)
                // 即使尚未执行字段排序规则修正，也不会将 Abc123 错认成 abc123。
                .apply("BINARY compression_code = {0}", compressionCode));
        if (urlMap == null) {
            cacheResult(compressionCode, Boolean.FALSE, MISSING_TTL_SECONDS);
            throw new ShortUrlNotFoundException();
        }
        cacheResult(compressionCode, urlMap, CACHE_TTL_SECONDS);
        return urlMap;
    }

    @Override
    public void editShortUrl(Long id, CreateShortUrlRequest request) {
        if (request == null) {
            throw new BusinessException("修改参数不能为空");
        }
        validate(request);
        String code = transactionTemplate.execute(status -> {
            UrlMap current = requiredMapping(id);
            int updated = baseMapper.update(null, Wrappers.<UrlMap>lambdaUpdate()
                    .eq(UrlMap::getId, id)
                    .set(UrlMap::getLongUrl, request.getLongUrl())
                    .set(UrlMap::getLongUrlDigest, DigestUtil.sha1Hex(request.getLongUrl()))
                    .set(UrlMap::getDescription, request.getDescription()));
            if (updated != 1) {
                throw new BusinessException("短链修改失败");
            }
            return current.getCompressionCode();
        });
        invalidateCache(code);
    }

    @Override
    public void changeShortUrlStatus(Long id, Integer status) {
        if (!Objects.equals(status, 1) && !Objects.equals(status, 2)) {
            throw new BusinessException("URL状态只能为1或2");
        }
        String code = transactionTemplate.execute(transaction -> {
            UrlMap current = requiredMapping(id);
            int updated = baseMapper.update(null, Wrappers.<UrlMap>lambdaUpdate()
                    .eq(UrlMap::getId, id).set(UrlMap::getUrlStatus, status));
            if (updated != 1) {
                throw new BusinessException("短链状态修改失败");
            }
            return current.getCompressionCode();
        });
        invalidateCache(code);
    }

    @Override
    public void deleteShortUrl(Long id) {
        String code = transactionTemplate.execute(transaction -> {
            UrlMap current = requiredMapping(id);
            if (baseMapper.deleteById(id) != 1) {
                throw new BusinessException("短链删除失败");
            }
            return current.getCompressionCode();
        });
        invalidateCache(code);
    }

    private UrlMap requiredMapping(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("短链ID必须大于0");
        }
        UrlMap mapping = baseMapper.selectById(id);
        if (mapping == null) {
            throw new ShortUrlNotFoundException();
        }
        return mapping;
    }

    private void invalidateCache(String code) {
        // TransactionTemplate 已返回，数据库事务已经提交。缓存删除失败时再尝试。
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                redisCache.deleteObject(CACHE_PREFIX + code);
                return;
            } catch (RuntimeException exception) {
                if (attempt == 2) {
                    log.error("数据库已提交但缓存删除失败，依赖 TTL 收敛，短码：{}", code, exception);
                }
            }
        }
    }

    private void cacheResult(String code, Object value, int ttlSeconds) {
        try {
            redisCache.setCacheObject(CACHE_PREFIX + code, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (RuntimeException exception) {
            // 数据库保存已成功，缓存失败不应使用户误以为创建失败。
            log.warn("写入短链缓存失败，后续请求可回查数据库，短码：{}", code, exception);
        }
    }

    private void validate(Object value) {
        Set<ConstraintViolation<Object>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct().sorted().collect(Collectors.joining("；"));
            throw new BusinessException(message);
        }
    }

    private void validateDomain(DomainConf domain) {
        validate(domain);
        if (!"http".equals(domain.getProtocol()) && !"https".equals(domain.getProtocol())) {
            throw new BusinessException("短链域名协议必须为http或https");
        }
        try {
            URI uri = URI.create(domain.getProtocol() + "://" + domain.getDomainValue());
            if (uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || !uri.getRawPath().isEmpty() || uri.getPort() > 65535) {
                throw new IllegalArgumentException("域名格式不正确");
            }
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("短链域名格式不正确");
        }
    }
}
