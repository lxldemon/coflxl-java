package com.coflxl.api.core.domain.entity;

import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Entity(tableName = "sys_leave_request")
@Data
public class SysLeaveRequest implements Serializable {
    @Id(strategy = "identity")
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "days")
    private Integer days;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Date createdAt;
}
