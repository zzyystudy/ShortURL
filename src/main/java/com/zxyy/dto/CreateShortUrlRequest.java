package com.zxyy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/** 创建时只接收用户可填写的字段，短码、摘要和状态由服务端生成。 */
@Data
public class CreateShortUrlRequest {

    @NotBlank(message = "长网址不能为空")
    @Size(max = 768, message = "长网址长度不能超过768个字符")
    @URL(regexp = "(?i)^https?://[^\\s/?#]+(?:[/?#][^\\s]*)?$",
            message = "长网址必须是合法的HTTP或HTTPS地址")
    private String longUrl;

    @Size(max = 256, message = "描述长度不能超过256个字符")
    private String description;
}
