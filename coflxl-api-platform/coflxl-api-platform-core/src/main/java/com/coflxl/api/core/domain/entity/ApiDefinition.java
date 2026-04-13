package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(tableName = "sys_api_definition")
public class ApiDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "api_code")
    private String apiCode;

    @Column(name = "api_name")
    private String apiName;

    @Column(name = "api_path")
    private String apiPath;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "operation_type")
    private String operationType; // QUERY / INSERT / UPDATE / DELETE

    @Column(name = "data_source_code")
    private String dataSourceCode;

    @Column(name = "system_code")
    private String systemCode;

    @Column(name = "execute_mode")
    private String executeMode; // SQL / SCRIPT / ADAPTER

    @Column(name = "status")
    private String status; // DRAFT / PUBLISHED / OFFLINE

    @Column(name = "auth_type")
    private String authType; // NONE / APP / TOKEN / SIGN

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "cache_enabled")
    private Boolean cacheEnabled;

    @Column(name = "cache_ttl_seconds")
    private Integer cacheTtlSeconds;

    @Column(name = "current_version")
    private String currentVersion;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
