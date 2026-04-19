package com.coflxl.api.core.domain.entity;

import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Entity(tableName = "sys_wf_def")
@Data
public class SysWfDef implements Serializable {
    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type_code")
    private String typeCode;

    @Column(name = "xml_data")
    private String xmlData;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Date createdAt;
}
