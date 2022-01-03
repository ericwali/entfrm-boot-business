package com.entfrm.biz.workflow.service.impl;

import com.entfrm.biz.workflow.service.WorkflowBpmnModelService;
import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * bpmn模型相关 service
 * </p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2022/1/4
 */
@Service
@AllArgsConstructor
public class WorkflowBpmnModelServiceImpl implements WorkflowBpmnModelService {


    private final RepositoryService repositoryService;


    /** 获取Bpmn模型 */
    @Override
    public BpmnModel getBpmnModelByProcessDefId(String processDefId) {
        return repositoryService.getBpmnModel(processDefId);
    }



    /** 查找结束事件的元素 */
    @Override
    public List<EndEvent> findEndFlowElement(String processDefId) {
        BpmnModel bpmnModel = this.getBpmnModelByProcessDefId(processDefId);
        if (bpmnModel != null) {
            Process process = bpmnModel.getMainProcess();
            return process.findFlowElementsOfType(EndEvent.class);
        } else {
            return null;
        }
    }



}