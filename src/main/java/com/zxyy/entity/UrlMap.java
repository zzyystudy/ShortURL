package com.zxyy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;

/**
 * URL 映射表：保存短码与长网址的对应关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("url_map")
public class UrlMap extends BaseEntity {

    /** 完整短网址。 */
    @Size(max = 32, message = "短网址长度不能超过32个字符")
    @URL(regexp = "(?i)^https?://[^\\s/?#]+(?:[/?#][^\\s]*)?$",
            message = "短网址必须是合法的HTTP或HTTPS地址")
    private String shortUrl;

    /** 目标长网址。 */
    @NotBlank(message = "长网址不能为空")
    @Size(max = 768, message = "长网址长度不能超过768个字符")
    @URL(regexp = "(?i)^https?://[^\\s/?#]+(?:[/?#][^\\s]*)?$",
            message = "长网址必须是合法的HTTP或HTTPS地址")
    private String longUrl;

    /** 短网址的 SHA-1 摘要。 */
    @Size(max = 128, message = "短网址摘要长度不能超过128个字符")
    private String shortUrlDigest;

    /** 长网址的 SHA-1 摘要。 */
    @Size(max = 128, message = "长网址摘要长度不能超过128个字符")
    private String longUrlDigest;

    /** 短码。 */
    @Size(max = 16, message = "短码长度不能超过16个字符")
    @Pattern(regexp = "[0-9a-zA-Z]+", message = "短码只能包含数字和大小写字母")
    private String compressionCode;

    /** 业务描述。 */
    @Size(max = 256, message = "描述长度不能超过256个字符")
    private String description;

    /** URL 状态：1 正常，2 已失效。 */
    @Min(value = 1, message = "URL状态只能为1或2")
    @Max(value = 2, message = "URL状态只能为1或2")
    private Integer urlStatus;
}
