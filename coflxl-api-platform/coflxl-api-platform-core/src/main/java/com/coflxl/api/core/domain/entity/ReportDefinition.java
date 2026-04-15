package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(tableName = "sys_report_definition")
public class ReportDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "data_source_code")
    private String dataSourceCode;

    @Column(name = "sql_query")
    private String sqlQuery;

    @Column(name = "parameters_json")
    private String parametersJson;

    @Column(name = "visualization_config_json")
    private String visualizationConfigJson;

    @Column(name = "layout_config_json")
    private String layoutConfigJson;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
