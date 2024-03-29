package com.entfrm.biz.workflow.controller;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.entfrm.base.api.R;
import com.entfrm.biz.extension.entity.WorkflowCopy;
import com.entfrm.biz.extension.service.WorkflowCopyService;
import com.entfrm.biz.workflow.entity.Workflow;
import com.entfrm.biz.workflow.enums.ExtendMessage;
import com.entfrm.biz.workflow.service.WorkflowProcessService;
import com.entfrm.biz.workflow.vo.ProcessDefinitionInfoVo;
import com.entfrm.biz.workflow.vo.ProcessInstanceInfoVo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 *<p>
 * 流程定义 controller
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2022/1/4
 */
@RestController
@AllArgsConstructor
@RequestMapping("/workflow/process")
public class WorkflowProcessController {

    private final TaskService taskService;

    private final WorkflowProcessService workflowProcessService;

    private final HistoryService historyService;

    private final WorkflowCopyService workflowCopyService;

    /** 流程定义列表 */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        IPage<ProcessDefinitionInfoVo> result = workflowProcessService.list(params);
        return R.ok(result.getRecords(), result.getTotal());
    }

    /** 流程定义是否存在 */
    @GetMapping("/exist/{processDefKey}")
    public R processDefinitionExist(@PathVariable String processDefKey) {
        ProcessDefinition processDefinition = workflowProcessService.getProcessDefinitionByKey(processDefKey);
        return R.ok(processDefinition == null ? "0" : "1");
    }

    /** 运行中的流程实例列表 */
    @GetMapping("/runList")
    public R runList(@RequestParam Map<String, Object> params) {
        IPage<ProcessInstanceInfoVo> result = workflowProcessService.runList(params);
        return R.ok(result.getRecords(), result.getTotal());
    }

    /** 历史流程列表 */
    @GetMapping("/historyList")
    public R historyList(@RequestParam Map<String, Object> params) {
        IPage<ProcessInstanceInfoVo> result = workflowProcessService.historyList(params);
        return R.ok(result.getRecords(), result.getTotal());
    }

    /** 读取xml/image资源 */
    @GetMapping("/resource")
    public ResponseEntity resource(String processInsId, String processDefId, String fileType) {

        HttpHeaders headers = new HttpHeaders();

        if ("xml".equals(fileType)) {
            headers.setContentType(MediaType.APPLICATION_XML);
        } else {
            headers.setContentType(MediaType.TEXT_XML);
        }

        InputStream resourceAsStream = workflowProcessService.readResource(processInsId, processDefId, fileType);
        return new ResponseEntity(IoUtil.readBytes(resourceAsStream), headers, HttpStatus.CREATED);
    }

    /** 获取bpmn.js建模器流程图xml */
    @GetMapping("/getFlowChart/{processDefId}")
    public R getFlowChart(@PathVariable String processDefId) {
        InputStream resourceAsStream = workflowProcessService.readResource(null, processDefId, "xml");
        return R.ok(new String(IoUtil.readBytes(resourceAsStream)));
    }

    /** 设置流程分类 */
    @PutMapping("/setProcessCategory")
    public R setProcessCategory(String[] processDefKeys, String category) {
        Arrays.asList(processDefKeys).forEach(processDefKey -> {
            ProcessDefinition processDefinition = workflowProcessService.getProcessDefinitionByKey(processDefKey);
            workflowProcessService.setProcessInstanceCategory(processDefinition.getId(), category);
        });
        return R.ok("流程分类设置成功!");
    }

    /** 挂起、激活流程实例 */
    @PutMapping("/setProcessInstanceStatus")
    public R setProcessInstanceStatus(String[] processDefKeys, String status) {
        Arrays.asList(processDefKeys).forEach(processDefKey -> {
            ProcessDefinition processDefinition = workflowProcessService.getProcessDefinitionByKey(processDefKey);
            workflowProcessService.setProcessInstanceStatus(processDefinition.getId(), status);
        });
        return R.ok();
    }

    /** 删除部署的流程 */
    @DeleteMapping("/removeDeployment/{deploymentIds}")
    public R removeDeployment(@PathVariable String[] deploymentIds) {
        Arrays.asList(deploymentIds).forEach(id -> workflowProcessService.deleteDeployment(id));
        return R.ok("删除成功");
    }

    /** 删除流程实例 */
    @DeleteMapping("/removeProcessInstance")
    public R removeProcessInstance(String[] processInsIds, String reason) {
        Arrays.asList(processInsIds).forEach(id -> workflowProcessService.deleteProcessInstance(id, reason));
        return R.ok("删除成功");
    }

    /** 流程撤回 */
    @PutMapping("/undoProcessInstance/{processInsId}")
    public R undoProcessInstance(@PathVariable String processInsId) {
        workflowProcessService.undoProcessInstance(processInsId);
        return R.ok("流程撤销成功!");
    }

    /** 流程终止 */
    @PutMapping("/stopProcessInstance")
    public R stopProcessInstance(String processInsId, String message) {
        workflowProcessService.stopProcessInstance(processInsId, ExtendMessage.PROCESS_STOP, message);
        return R.ok("终止流程成功!");
    }

    /** 查询流程状态 */
    @GetMapping("/queryProcessStatus/{processInsId}")
    public R queryProcessStatus(@PathVariable String processInsId) {
        ProcessInstanceInfoVo processInstanceInfo = workflowProcessService.queryProcessState(processInsId);
        return R.ok(processInstanceInfo);

    }

    /** 自己发起流程实例列表 */
    @GetMapping("/selfProcessInstanceList")
    public R selfProcessInstanceList(@RequestParam Map<String, Object> params) {
        IPage<ProcessInstanceInfoVo> result = workflowProcessService.selfProcessInstanceList(params);
        return R.ok(result.getRecords(), result.getTotal());
    }

    /** 启动流程定义 */
    @PostMapping("/startProcessDefinition")
    public R startProcessDefinition(@RequestBody Workflow workflow) {
        String processInsId = workflowProcessService.startProcessDefinition(
                workflow.getProcessDefKey(),
                workflow.getBusinessTable(),
                workflow.getBusinessId(),
                workflow.getTitle());

        // 指定下一步处理人,不设置就使用默认处理人
        if (StringUtils.isNotBlank(workflow.getAssignee())) {
            Task task = taskService.createTaskQuery().processInstanceId(processInsId).active().singleResult();
            if (task != null) {
                taskService.setAssignee(task.getId(), workflow.getAssignee());
            }
        }
        return R.ok(processInsId, "启动成功");
    }

    /** 删除历史流程实例 */
    @DeleteMapping("/removeHistoryProcessIns/{ids}")
    public R removeHistoryProcessIns(@PathVariable String[] ids) {
       Arrays.asList(ids).forEach(id ->{
           historyService.deleteHistoricProcessInstance(id);
           workflowCopyService.remove(new LambdaUpdateWrapper<WorkflowCopy>().eq(WorkflowCopy::getProcessInsId, id));
       });
       return R.ok("删除成功，流程实例ID=" + ids);
    }

}
