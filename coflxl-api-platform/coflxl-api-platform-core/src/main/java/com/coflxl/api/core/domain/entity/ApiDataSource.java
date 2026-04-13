package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(tableName = "sys_api_data_source")
public class ApiDataSource implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "db_type")
    private String dbType;

    @Column(name = "jdbc_url")
    private String jdbcUrl;

    @Column(name = "username")
    private String username;

    @Column(name = "password_encrypted")
    private String passwordEncrypted;

    @Column(name = "driver_class_name")
    private String driverClassName;

    @Column(name = "read_only_flag")
    private Boolean readOnlyFlag;

    @Column(name = "pool_config_json")
    private String poolConfigJson;

    @Column(name = "system_code")
    private String systemCode;

    @Column(name = "status")
    private String status;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
