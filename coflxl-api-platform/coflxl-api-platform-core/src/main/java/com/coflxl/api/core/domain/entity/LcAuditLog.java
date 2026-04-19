package com.coflxl.api.core.domain.entity;

import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "lc_audit_log")
public class LcAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "business_id", length = 64)
    private String businessId;

    @Column(name = "task_id", length = 64)
    private String taskId;

    @Column(name = "task_name", length = 128)
    private String taskName;

    @Column(name = "assignee", length = 64)
    private String assignee;

    @Column(name = "action", length = 20)
    private String action;

    @Column(name = "comment")
    private String comment;

    @Column(name = "create_time")
    private Date createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
