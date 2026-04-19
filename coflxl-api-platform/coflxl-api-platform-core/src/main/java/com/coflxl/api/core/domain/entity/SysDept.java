package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity(tableName = "sys_dept")
public class SysDept implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name")
    private String name;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Date createdAt;
}
