package com.zxyy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * 短链转换事件记录表：保存每次访问的明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transform_event_record")
public class TransformEventRecord extends BaseEntity {

    /** 近似访客标识：SHA-1(客户端 IP + "-" + User-Agent)。 */
    @Size(max = 128, message = "访客标识长度不能超过128个字符")
    private String uniqueIdentity;

    /** 客户端 IP。 */
    @NotBlank(message = "客户端IP不能为空")
    @Size(max = 64, message = "客户端IP长度不能超过64个字符")
    private String clientIp;

    /** 短网址。 */
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
    @NotBlank(message = "短码不能为空")
    @Size(max = 16, message = "短码长度不能超过16个字符")
    @Pattern(regexp = "[0-9a-zA-Z]+", message = "短码只能包含数字和大小写字母")
    private String compressionCode;

    /** 访问事件发生时间，与数据库插入时间 createTime 区分。 */
    @NotNull(message = "访问时间不能为空")
    private LocalDateTime recordTime;

    /** User-Agent 请求头。 */
    @Size(max = 2048, message = "User-Agent长度不能超过2048个字符")
    private String userAgent;

    /** Cookie 请求头。 */
    @Size(max = 2048, message = "Cookie长度不能超过2048个字符")
    private String cookieValue;

    /** URL 查询参数。 */
    @Size(max = 2048, message = "查询参数长度不能超过2048个字符")
    private String queryParam;

    /** 省份。 */
    @Size(max = 32, message = "省份长度不能超过32个字符")
    private String province;

    /** 城市。 */
    @Size(max = 32, message = "城市长度不能超过32个字符")
    private String city;

    /** 手机型号。 */
    @Size(max = 64, message = "手机型号长度不能超过64个字符")
    private String phoneType;

    /** 浏览器类型。 */
    @Size(max = 64, message = "浏览器类型长度不能超过64个字符")
    private String browserType;

    /** 浏览器版本。 */
    @Size(max = 128, message = "浏览器版本长度不能超过128个字符")
    private String browserVersion;

    /** 操作系统类型。 */
    @Size(max = 32, message = "操作系统类型长度不能超过32个字符")
    private String osType;

    /** 设备类型。 */
    @Size(max = 32, message = "设备类型长度不能超过32个字符")
    private String deviceType;

    /** 操作系统版本。 */
    @Size(max = 32, message = "操作系统版本长度不能超过32个字符")
    private String osVersion;

    /** 转换状态：0 未知，1 转换成功，2 转换失败，3 重定向成功，4 重定向失败。 */
    @Min(value = 0, message = "转换状态必须在0到4之间")
    @Max(value = 4, message = "转换状态必须在0到4之间")
    private Integer transformStatus;
}
