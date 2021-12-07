package com.entfrm.biz.flowable.service;

import com.entfrm.biz.flowable.model.Flow;
import com.entfrm.biz.flowable.dto.FormDataDto;

/**
 * <p>
 * 动态表单表单相关 service
 * </p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021-05-16
 */
public interface FlowableFormService {


    /**
     * 动态表单专用
     * processDefinitionId:启动流程定义唯一Id
     * title:流程标题
     * data:需要存入流程变量的表单数据,必须
     *@return :当前正在运行的流程实例ID
     */
    String submitStartFormData(String processDefinitionId,
                               String title,
                               String data);


    /**
     * 动态表单审批专用
     * 审批任务
     * flow:工作流通用数据传输VO
     * data:需要存入流程变量的表单数据,必须
     * **/
    void submitTaskFormData(Flow flow, String data);



    /**
     * 获取表单数据
     * code:表单编码
     * procInsId:流程实例ID
     */
    FormDataDto getFormData(String code, String procInsId);



}
