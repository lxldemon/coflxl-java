package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;

@Data
@Entity(tableName = "sys_api_param_definition")
public class ApiParamDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;
    
    @Column(name = "api_code")
    private String apiCode;
    
    @Column(name = "param_code")
    private String paramCode;
    
    @Column(name = "param_name")
    private String paramName;
    
    @Column(name = "source_type")
    private String sourceType; // QUERY / PATH / HEADER / BODY
    
    @Column(name = "data_type")
    private String dataType; // STRING / LONG / INTEGER / DECIMAL / BOOLEAN / DATE / JSON
    
    @Column(name = "required_flag")
    private Boolean requiredFlag;
    
    @Column(name = "default_value")
    private String defaultValue;
    
    @Column(name = "regex_rule")
    private String regexRule;
    
    @Column(name = "sort_no")
    private Integer sortNo;
}
