package com.entfrm.biz.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.entfrm.data.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author entfrm
 * @since 2019-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_menu")
public class Menu extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    @TableId
    private Integer id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 父菜单ID
     */
    private Integer parentId;

    /**
     * 所属应用
     */
    private Integer applicationId;

    /**
     * 父菜单IDS
     */
    private String parentIds;

    /**
     * 父菜单名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 菜单类型（M模块 C菜单 F资源）
     */
    private String type;

    /**
     * 菜单地址
     */
    private String path;
    /**
     * 组件（前后端分离预留）
     */
    private String component;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 路由缓存
     */
    private String noCache;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    private String status;

    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<Menu> children = new ArrayList<Menu>();


}
