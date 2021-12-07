/**
 * Copyright © 2015-2020 <a href="http://www.jeeplus.org/">JeePlus</a> All rights reserved.
 */
package com.entfrm.biz.flowable.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 *<p>
 * 常用按钮
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021/6/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("act_extension_button")
public class Button{

	/** id **/
	@TableId
	private Integer id;
	/** 名称 **/
	private String name;
	/** 编码 **/
	private String code;
	/** 排序 **/
	private String sort;
	
}