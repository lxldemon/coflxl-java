package com.coflxl.api.core.domain.entity;

import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "lc_business_instance")
public class LcBusinessInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "form_id", length = 64)
    private String formId;

    @Column(name = "proc_ins_id", length = 64)
    private String procInsId;

    @Column(name = "data_content")
    private String dataContent;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "creator", length = 64)
    private String creator;

    @Column(name = "create_time")
    private Date createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFormId() { return formId; }
    public void setFormId(String formId) { this.formId = formId; }
    public String getProcInsId() { return procInsId; }
    public void setProcInsId(String procInsId) { this.procInsId = procInsId; }
    public String getDataContent() { return dataContent; }
    public void setDataContent(String dataContent) { this.dataContent = dataContent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
