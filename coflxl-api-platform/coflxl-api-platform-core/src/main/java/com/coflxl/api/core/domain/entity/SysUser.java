package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import org.sagacity.sqltoy.config.annotation.SqlToyEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(tableName = "sys_user")
public class SysUser implements Serializable {
    @Id(strategy = "identity")
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
