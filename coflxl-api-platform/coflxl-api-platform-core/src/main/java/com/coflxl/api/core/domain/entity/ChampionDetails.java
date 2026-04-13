package com.coflxl.api.core.domain.entity;

import lombok.Data;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;

/**
 * ChampionDetails
 * @author coflxl
 */
@Data
@Entity(tableName = "champion_details")
public class ChampionDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "heroId")
    @Id
    private Integer heroid;

    @Column(name = "name")
    private String name;

    @Column(name = "title")
    private String title;

    @Column(name = "roles")
    private String roles;

    @Column(name = "lane")
    private String lane;

    @Column(name = "intro")
    private String intro;

    @Column(name = "description")
    private String description;

    @Column(name = "skills_json")
    private String skillsJson;

    @Column(name = "avatar_url")
    private String avatarUrl;


}
