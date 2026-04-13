package com.coflxl.api.core.domain.entity;

import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(tableName = "sys_api_system")
public class ApiSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "system_code")
    private String systemCode;

    @Column(name = "system_name")
    private String systemName;

    @Column(name = "dev_engineer")
    private String devEngineer;

    @Column(name = "ss_engineer")
    private String ssEngineer;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getDevEngineer() {
        return devEngineer;
    }

    public void setDevEngineer(String devEngineer) {
        this.devEngineer = devEngineer;
    }

    public String getSsEngineer() {
        return ssEngineer;
    }

    public void setSsEngineer(String ssEngineer) {
        this.ssEngineer = ssEngineer;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
