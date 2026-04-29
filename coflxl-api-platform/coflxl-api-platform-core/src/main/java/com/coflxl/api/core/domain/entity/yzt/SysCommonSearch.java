package com.coflxl.api.core.domain.entity.yzt;


import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;

/**
 * SysCommonSearch
 * @author coflxl
 */
@Data
@Entity(tableName = "SYS_COMMON_SEARCH")
public class SysCommonSearch implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id(strategy = "snowflake", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "SQL_ID")
    private String sqlId;

    @Column(name = "SQL_DESC")
    private String sqlDesc;

    @Column(name = "CREATE_BY")
    private Long createBy;

    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;

    @Column(name = "UPDATE_BY")
    private Long updateBy;

    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;

    @Column(name = "IS_DELETED")
    private Integer isDeleted;

    @Column(name = "REVISION")
    private Integer revision;

    @Column(name = "SQL_CONTENT")
    private String sqlContent;


}
