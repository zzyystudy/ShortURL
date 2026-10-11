package com.zxyy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 短链每日访问统计表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visit_statistics")
public class VisitStatistics extends BaseEntity {

    /** 统计日期。 */
    @NotNull(message = "统计日期不能为空")
    private LocalDate statisticsDate;

    /** 页面访问记录数。 */
    @PositiveOrZero(message = "访问次数不能为负数")
    private Long pvCount;

    /** 近似独立访客数。 */
    @PositiveOrZero(message = "独立访客数不能为负数")
    private Long uvCount;

    /** 独立 IP 数。 */
    @PositiveOrZero(message = "独立IP数不能为负数")
    private Long ipCount;

    /** 有效跳转数。 */
    @PositiveOrZero(message = "有效跳转数不能为负数")
    private Long effectiveRedirectionCount;

    /** 无效跳转数。 */
    @PositiveOrZero(message = "无效跳转数不能为负数")
    private Long ineffectiveRedirectionCount;

    /** 短码。 */
    @NotBlank(message = "短码不能为空")
    @Size(max = 16, message = "短码长度不能超过16个字符")
    @Pattern(regexp = "[0-9a-zA-Z]+", message = "短码只能包含数字和大小写字母")
    private String compressionCode;

    /** 短网址的 SHA-1 摘要。 */
    @Size(max = 128, message = "短网址摘要长度不能超过128个字符")
    private String shortUrlDigest;

    /** 长网址的 SHA-1 摘要。 */
    @Size(max = 128, message = "长网址摘要长度不能超过128个字符")
    private String longUrlDigest;
}
