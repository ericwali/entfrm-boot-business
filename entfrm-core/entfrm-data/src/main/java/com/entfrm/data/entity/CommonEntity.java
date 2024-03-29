package com.entfrm.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 *<p>
 * 通用模型
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021/12/28
 */
@Data
public class CommonEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    protected String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    protected String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date updateTime;

    /** 备注 */
    protected String remarks;

    /** 删除标志（0代表存在 1代表删除）*/
    @TableLogic
    @JsonIgnore
    protected String delFlag;

    /** 开始时间 */
    @TableField(exist = false)
    @JsonIgnore
    private String beginTime;

    /** 结束时间 */
    @TableField(exist = false)
    @JsonIgnore
    private String endTime;

}
