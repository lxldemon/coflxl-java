package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;

@Data
@Entity(tableName = "sys_menu")
public class SysMenu implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "component")
    private String component;

    @Column(name = "icon")
    private String icon;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(name = "visible_flag")
    private Boolean visibleFlag;

    @Column(name = "keep_alive_flag")
    private Boolean keepAliveFlag;

    @Column(name = "type_flag")
    private String typeFlag;

    @Column(name = "permission_code")
    private String permissionCode;
}
