package com.entfrm.biz.workflow.controller;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.entfrm.base.api.R;
import com.entfrm.biz.extension.entity.ActivityExtensionProperty;
import com.entfrm.biz.extension.service.ActivityExtensionPropertyService;
import com.entfrm.biz.workflow.entity.Workflow;
import com.entfrm.biz.workflow.service.WorkflowProcessService;
import com.entfrm.biz.workflow.service.WorkflowTaskService;
import com.entfrm.biz.workflow.vo.ActivityCommentInfoVo;
import com.entfrm.biz.workflow.vo.HistoryTaskInfoVo;
import com.entfrm.biz.workflow.vo.ProcessInstanceInfoVo;
import com.entfrm.security.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *<p>
 * 流程任务 controller
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2022/1/4
 */
@RestController
@AllArgsConstructor
@RequestMapping("/workflow/task")
public class WorkflowTaskController {

    private final WorkflowTaskService workflowTaskService;

    private final ActivityExtensionPropertyService activityExtensionPropertyService;

    private final WorkflowProcessService workflowProcessService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RuntimeService runtimeService;

    /** 代办任务列表 */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        IPage<ProcessInstanceInfoVo> result = workflowTaskService.list(params);
        return R.ok(result.getRecords(), result.getTotal());
    }

    /** 已办流转任务列表 */
    @GetMapping("/historyFlowChangeList/{processInsId}")
    public R historyFlowChangeList(@PathVariable String processInsId) {
        List<Workflow> result = workflowTaskService.historyFlowChangeList(processInsId);
        return R.ok(result);
    }

    /** 已办任务列表 */
    @GetMapping("/historyList")
    public R historyList(@RequestParam Map<String, Object> params) {
        IPage<HistoryTaskInfoVo> result = workflowTaskService.historyList(params);
        return R.ok(result.getRecords(), result.getTotal());
    }

    /** 获取任务定义 */
    @GetMapping("/getTaskDefinition")
    public R getTaskDefinition(Workflow workflow) {
        // 获取流程XML上的表单KEY
        String formKey = workflowTaskService.getTaskFormKey(workflow.getProcessDefId(), workflow.getTaskDefKey());

        ActivityExtensionProperty formTypeNode = activityExtensionPropertyService.getOne(new LambdaQueryWrapper<ActivityExtensionProperty>()
                .eq(ActivityExtensionProperty::getProcessDefId, workflow.getProcessDefId())
                .eq(ActivityExtensionProperty::getActivityDefId,workflow.getTaskDefKey())
                .eq(ActivityExtensionProperty::getKey, "formType"));

        ActivityExtensionProperty formReadOnlyNode = activityExtensionPropertyService.getOne(new LambdaQueryWrapper<ActivityExtensionProperty>()
                .eq(ActivityExtensionProperty::getProcessDefId, workflow.getProcessDefId())
                .eq(ActivityExtensionProperty::getActivityDefId, workflow.getTaskDefKey())
                .eq(ActivityExtensionProperty::getKey, "formReadOnly"));

        // 处理拿到表单核心数据
        String formType = "1";
        boolean formReadOnly = false;
        if (formTypeNode != null) {
            formType = formTypeNode.getValue();
            formReadOnly = "true".equals(formReadOnlyNode.getValue());
        } else {
            if(StrUtil.isBlank(formKey)) {
                formType = "1";
            } else  if (formKey.matches("^/[\\w\\W]+$")) {
                formType = "2";
            }
        }

        // 获取流程实例对象
        if (workflow.getProcessInsId() != null) {
            ProcessInstance processInstance = workflowProcessService.getProcessInstance(workflow.getProcessInsId());
            if (processInstance != null) {
                workflow.setProcessInstanceBusinessData(processInstance);
            } else {
                workflow.setFinishedProcessInstanceBusinessData(workflowProcessService.getFinishedProcessInstance(workflow.getProcessInsId()));
            }
        }

        workflow.setFormKey(formKey);
        workflow.setFormReadOnly(formReadOnly);
        workflow.setFormType(formType);
        return R.ok(workflow);
    }

    /** 提交任务 */
    @PostMapping("/complete")
    public R complete(@RequestBody Workflow workflow) {
       workflowTaskService.complete(workflow);
       return R.ok("完成任务!");
    }

    /** 获取bpmn.js绘制颜色xml */
    @GetMapping("/getPaintDiagramXml/{processInsId}")
    public Map getPaintDiagramXml(@PathVariable String processInsId) {
        return workflowTaskService.getPaintDiagramXml(processInsId);
    }

    /** 删除任务 */
    @DeleteMapping("/removeTask")
    public R removeTask(String taskId, String reason) {
        if (StringUtils.isBlank(reason)) {
            return R.error("请填写删除原因");
        } else {
            workflowTaskService.removeTask(taskId, reason);
            return R.ok("删除任务成功，任务ID=" + taskId);
        }
    }

    /** 审批任务 */
    @PostMapping("/auditTask")
    public R auditTask(@RequestBody Workflow workflow) {
        workflowTaskService.auditTask(workflow);
        //指定下一步处理人
        if(StringUtils.isNotBlank(workflow.getAssignee())){
            Task task = taskService.createTaskQuery().processInstanceId(workflow.getProcessInsId()).active().singleResult();
            if(task != null){
                taskService.setAssignee(task.getId(), workflow.getAssignee());
            }
        }
        return R.ok("审批成功");
    }

    /** 撤销任务 */
    @PostMapping("/undoTask")
    public R undoTask(@RequestBody Map<String,Object> params) {
        try {
            String undoTaskId = MapUtil.getStr(params, "undoTaskId"),
                   currentTaskId= MapUtil.getStr(params, "currentTaskId"),
                   processInsId = MapUtil.getStr(params, "processInsId"),
                   undoTaskDefKey = MapUtil.getStr(params, "undoTaskDefKey"),
                   currentTaskDefKey = MapUtil.getStr(params, "currentTaskDefKey");

            // 取得流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInsId).singleResult();
            if (processInstance == null) return R.error("流程已经结束");

            //在已办任务列表中清除该任务信息
            historyService.deleteHistoricTaskInstance(undoTaskId);

            List<String> rollBackTaskList = ListUtil.toList(currentTaskDefKey);
            // 回退到上一节点
            // 这里可以使用也可以使用自己封装的rollBackTask,但是不能退回至子流程中任意节点
            // 如果想要实现,可以使用flowable的moveActivityIdToSubProcessInstanceActivityId
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getId())
                    .moveActivityIdsToSingleActivityId(rollBackTaskList, undoTaskDefKey).changeState();
            historyService.deleteHistoricTaskInstance(currentTaskId);
            return R.ok("取回成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.ok("流程取回失败，未知错误.");
        }
    }

    /** 委托任务 */
    @PostMapping("/delegateTask")
    public R delegateTask(String taskId, String userId) {
        if (StringUtils.isBlank(taskId) || StringUtils.isBlank(userId)) return R.error("参数异常");
        taskService.setOwner(taskId, SecurityUtil.getUser().getId() + "");
        // 设置任务委托人为的指定的处理人
        taskService.delegateTask(taskId, userId);
        return R.ok("委托成功");
    }

    /** 签收任务 */
    @PostMapping("/claim/{taskId}")
    public R claim(@PathVariable String taskId) {
        workflowTaskService.claim(taskId, SecurityUtil.getUser().getId() + "");
        return R.ok("签收成功");
    }

    /** 取消签收任务 */
    @PostMapping("/cancelClaim/{taskId}")
    public R cancelClaim(@PathVariable String taskId) {
        taskService.unclaim(taskId);
        return R.ok("取消签收成功");
    }

    /** 转移任务 */
    @PostMapping("/transferTask")
    public R transferTask(String taskId, String userId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(taskId)) return R.error("转移失败,参数异常");
        // 设置当前流程任务办理人
        Authentication.setAuthenticatedUserId(SecurityUtil.getUser().getId() + "");
        taskService.setAssignee(taskId, userId);
        return R.ok("转移成功!");
    }

    /** 可回滚任务列表 */
    @GetMapping("/rollBackTaskList/{taskId}")
    public R rollBackTaskList(@PathVariable String taskId) {
        List<Workflow> nodes = workflowTaskService.rollBackTaskList(taskId);
        return R.ok(nodes);
    }

    /** 驳回任务 */
    @PostMapping("/rejectTask")
    public R rejectTask(@RequestBody Map<String,Object> params) {
        String rollBackTaskDefKey = MapUtil.getStr(params, "rollBackTaskDefKey"),
               currentTaskId= MapUtil.getStr(params, "currentTaskId");
        ActivityCommentInfoVo activityCommentInfo = MapUtil.get(params, "comment", ActivityCommentInfoVo.class);
        workflowTaskService.rollBackTask(rollBackTaskDefKey, currentTaskId, activityCommentInfo);
        return R.ok("驳回成功!");
    }

    /** 读取流程历史数据，用于渲染流程图 */
    @GetMapping("/getFlowChart/{processInsId}")
    public Map getFlowChart(@PathVariable String processInsId) {
        return workflowTaskService.getDiagram(processInsId);
    }

}
