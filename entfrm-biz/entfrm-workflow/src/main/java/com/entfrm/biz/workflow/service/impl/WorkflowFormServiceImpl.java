package com.entfrm.biz.workflow.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.entfrm.biz.workflow.constant.FlowableConstant;
import com.entfrm.biz.workflow.entity.Workflow;
import com.entfrm.biz.workflow.mapper.WorkflowMapper;
import com.entfrm.biz.workflow.service.WorkflowFormService;
import com.entfrm.biz.workflow.service.WorkflowProcessService;
import com.entfrm.biz.workflow.service.WorkflowTaskService;
import com.entfrm.security.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 动态表单表单相关 service
 * </p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2022/1/4
 */
@Service
@AllArgsConstructor
public class WorkflowFormServiceImpl implements WorkflowFormService {


    private final RuntimeService runtimeService;
    private final WorkflowTaskService workflowTaskService;
    private final WorkflowMapper workFlowMapper;
    private final WorkflowProcessService workflowProcessService;
    private final IdentityService identityService;


    /** 动态表单启动流程专用 */
    @Override
    public String submitStartFormData(String processDefinitionId, String title, String data) {

        //表单数据转换JSON对象
        JSONObject jData = JSONUtil.parseObj(data);
        //设置流程变量
        Map<String,Object> vars = MapUtil.newHashMap();
        String userName = SecurityUtil.getUser().getUsername();
        vars.put(FlowableConstant.USERNAME,userName);
        //设置流程发起人
        identityService.setAuthenticatedUserId(userName);

        // 设置流程标题
        if (StrUtil.isNotBlank(title)) {
            vars.put("title", title);
        }
        for(String key:jData.keySet()){
            vars.put(key,jData.getObj(key));
        }

        //启动流程实例
        String procInsId=runtimeService.startProcessInstanceById(processDefinitionId,vars).getProcessInstanceId();

        return procInsId;

    }



    /** 动态表单审核专用 */
    @Override
    public void submitTaskFormData(Workflow workFlow, String data) {

        //表单数据转换JSON对象
        JSONObject jData = JSONUtil.parseObj(data);

        //设置流程变量
        Map<String,Object> vars = MapUtil.newHashMap();
        for(String key:jData.keySet()){
            vars.put(key,jData.getObj(key));
        }
        workFlow.setVars(vars);
        workflowTaskService.complete(workFlow);

    }

}