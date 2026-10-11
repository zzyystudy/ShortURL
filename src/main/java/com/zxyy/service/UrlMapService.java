package com.zxyy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxyy.dto.CreateShortUrlRequest;
import com.zxyy.entity.UrlMap;

/**
 * URL 映射的创建和解析服务。
 */
public interface UrlMapService extends IService<UrlMap> {

    /** 校验配置、生成短码、保存映射，数据库提交后写入缓存。 */
    UrlMap createShortUrl(CreateShortUrlRequest request);

    /** 优先查缓存，未命中时查数据库；不存在或禁用时抛出异常。 */
    UrlMap resolveShortUrl(String compressionCode);

    /** 修改目标网址和描述，提交数据库后使缓存失效。 */
    void editShortUrl(Long id, CreateShortUrlRequest request);

    /** 1 启用、2 禁用，提交数据库后使缓存失效。 */
    void changeShortUrlStatus(Long id, Integer status);

    /** 逻辑删除，提交数据库后使缓存失效。 */
    void deleteShortUrl(Long id);
}
