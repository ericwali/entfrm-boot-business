package com.entfrm.biz.workflow.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.flowable.task.api.TaskInfo;

import java.util.Date;

/**
 *<p>
 * 当前活动任务节点信息
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021/5/12
 */
@Data
@Accessors(chain = true)
public class TaskInfoVo {

    private String id;
    private String name;
    private String assignee;
    private String executionId;
    private String taskDefinitionKey;
    private Date createTime;
    private String processDefinitionId;
    private String processInstanceId;
    private String processDefKey;


    public TaskInfoVo(TaskInfo task){
        this.id = task.getId ();
        this.name = task.getName ();
        this.assignee = task.getAssignee ();
        this.executionId = task.getExecutionId ();
        this.taskDefinitionKey = task.getTaskDefinitionKey ();
        this.createTime = task.getCreateTime ();
        this.processDefinitionId = task.getProcessDefinitionId ();
        this.processInstanceId = task.getProcessInstanceId ();
    }

}