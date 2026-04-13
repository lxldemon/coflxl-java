package com.coflxl.api.admin.controller;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ChampionDetails;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ChampionDetails Controller
 * @author coflxl
 */
@RestController
@RequestMapping("/admin/champion-details")
public class AdminChampionDetailsController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @PostMapping("/save")
    public ApiResponse<Boolean> save(@RequestBody ChampionDetails championDetails) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.saveOrUpdate(championDetails);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<ChampionDetails>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam Map<String, Object> params) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        org.sagacity.sqltoy.model.Page<ChampionDetails> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        StringBuilder sql = new StringBuilder("select * from champion_details where 1=1 ");
        if (params.get("heroid") != null && !params.get("heroid").toString().trim().isEmpty()) {
            sql.append(" and heroId = :heroid ");
        }
        if (params.get("name") != null && !params.get("name").toString().trim().isEmpty()) {
            sql.append(" and name like :name ");
            params.put("name", "%" + params.get("name") + "%");
        }
        if (params.get("title") != null && !params.get("title").toString().trim().isEmpty()) {
            sql.append(" and title like :title ");
            params.put("title", "%" + params.get("title") + "%");
        }
        if (params.get("roles") != null && !params.get("roles").toString().trim().isEmpty()) {
            sql.append(" and roles like :roles ");
            params.put("roles", "%" + params.get("roles") + "%");
        }
        if (params.get("lane") != null && !params.get("lane").toString().trim().isEmpty()) {
            sql.append(" and lane like :lane ");
            params.put("lane", "%" + params.get("lane") + "%");
        }
        if (params.get("intro") != null && !params.get("intro").toString().trim().isEmpty()) {
            sql.append(" and intro like :intro ");
            params.put("intro", "%" + params.get("intro") + "%");
        }
        if (params.get("description") != null && !params.get("description").toString().trim().isEmpty()) {
            sql.append(" and description like :description ");
            params.put("description", "%" + params.get("description") + "%");
        }
        if (params.get("skillsJson") != null && !params.get("skillsJson").toString().trim().isEmpty()) {
            sql.append(" and skills_json like :skillsJson ");
            params.put("skillsJson", "%" + params.get("skillsJson") + "%");
        }
        if (params.get("avatarUrl") != null && !params.get("avatarUrl").toString().trim().isEmpty()) {
            sql.append(" and avatar_url like :avatarUrl ");
            params.put("avatarUrl", "%" + params.get("avatarUrl") + "%");
        }


        org.sagacity.sqltoy.model.Page<ChampionDetails> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql.toString(), params, ChampionDetails.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("delete from champion_details where id = :id", Map.of("id", id));
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
