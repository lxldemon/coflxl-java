package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(tableName = "sys_report_instance")
public class ReportInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "definition_id")
    private Long definitionId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "data_source_code")
    private String dataSourceCode;

    @Column(name = "actual_parameters_json")
    private String actualParametersJson;

    @Column(name = "publish_status")
    private String publishStatus;

    @Column(name = "access_url")
    private String accessUrl;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
