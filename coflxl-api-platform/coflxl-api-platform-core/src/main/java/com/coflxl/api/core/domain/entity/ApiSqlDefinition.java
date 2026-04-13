package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(tableName = "sys_api_sql_definition")
public class ApiSqlDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;
    
    @Column(name = "api_code")
    private String apiCode;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "sql_text")
    private String sqlText;
    
    @Column(name = "sql_type")
    private String sqlType;
    
    @Column(name = "result_mapping_json")
    private String resultMappingJson;
    
    @Column(name = "safe_level")
    private Integer safeLevel;
    
    @Column(name = "enabled_flag")
    private Boolean enabledFlag;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
