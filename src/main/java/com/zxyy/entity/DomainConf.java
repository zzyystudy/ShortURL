package com.zxyy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短链域名配置表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("domain_conf")
public class DomainConf extends BaseEntity {

    /** 域名，可包含端口，例如 localhost:8778。 */
    @NotBlank(message = "域名不能为空")
    @Size(max = 16, message = "域名长度不能超过16个字符")
    private String domainValue;

    /** 协议：http 或 https。 */
    @Pattern(regexp = "https?", message = "协议只能为http或https")
    private String protocol;

    /** 域名状态：1 正常，2 已失效。 */
    @Min(value = 1, message = "域名状态只能为1或2")
    @Max(value = 2, message = "域名状态只能为1或2")
    private Integer domainStatus;
}
