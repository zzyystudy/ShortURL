package com.zxyy.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 四张业务表的公共字段。
 */
@Data
public abstract class BaseEntity {

    /** 数据库自增主键。 */
    @TableId(type = IdType.AUTO)
    @Positive(message = "主键必须大于0")
    private Long id;

    /** 创建时间，由数据库默认值生成，不参与插入和更新。 */
    @TableField(insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    /** 更新时间，由数据库 DEFAULT / ON UPDATE 维护。 */
    @TableField(insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime editTime;

    /** 创建者，未赋值时使用数据库默认值 admin。 */
    @Size(max = 32, message = "创建者长度不能超过32个字符")
    private String creator;

    /** 更新者，未赋值时使用数据库默认值 admin。 */
    @Size(max = 32, message = "更新者长度不能超过32个字符")
    private String editor;

    /** 逻辑删除标识：0 未删除，1 已删除。 */
    @TableLogic(value = "0", delval = "1")
    @Min(value = 0, message = "删除标识只能为0或1")
    @Max(value = 1, message = "删除标识只能为0或1")
    private Integer deleted;
}
