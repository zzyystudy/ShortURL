package com.zxyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 创建成功后返回的短链信息。 */
@Data
@AllArgsConstructor
public class CreateShortUrlResponse {

    private Long id;
    private String shortUrl;
    private String compressionCode;
    private String longUrl;
}
