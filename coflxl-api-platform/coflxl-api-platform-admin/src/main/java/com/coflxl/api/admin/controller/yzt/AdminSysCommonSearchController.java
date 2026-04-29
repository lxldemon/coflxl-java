package com.coflxl.api.admin.controller.yzt;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DataSourceManager;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.yzt.SysCommonSearch;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SysCommonSearch Controller
 * @author coflxl
 */
@RestController
@RequestMapping("/admin/SYS-COMMON-SEARCH")
public class AdminSysCommonSearchController {
    @Autowired
    private DataSourceManager dataSourceManager;
    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @PostMapping("/save")
    public ApiResponse<Boolean> save(@RequestBody SysCommonSearch sysCommonSearch) {
        dataSourceManager.ensureDataSource("db_yzt_config");
        DynamicDataSourceContextHolder.set("db_yzt_config");
        sqlToyLazyDao.saveOrUpdate(sysCommonSearch);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<SysCommonSearch>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam Map<String, Object> params) {
        dataSourceManager.ensureDataSource("db_yzt_config");
        DynamicDataSourceContextHolder.set("db_yzt_config");
        org.sagacity.sqltoy.model.Page<SysCommonSearch> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        StringBuilder sql = new StringBuilder("select * from SYS_COMMON_SEARCH where 1=1 ");
        if (params.get("sqlId") != null && !params.get("sqlId").toString().trim().isEmpty()) {
            sql.append(" and SQL_ID like :sqlId ");
            params.put("sqlId", "%" + params.get("sqlId") + "%");
        }
        if (params.get("sqlDesc") != null && !params.get("sqlDesc").toString().trim().isEmpty()) {
            sql.append(" and SQL_DESC like :sqlDesc ");
            params.put("sqlDesc", "%" + params.get("sqlDesc") + "%");
        }
        if (params.get("createBy") != null && !params.get("createBy").toString().trim().isEmpty()) {
            sql.append(" and CREATE_BY = :createBy ");
        }
        if (params.get("createTime") != null && !params.get("createTime").toString().trim().isEmpty()) {
            sql.append(" and CREATE_TIME = :createTime ");
        }
        if (params.get("updateBy") != null && !params.get("updateBy").toString().trim().isEmpty()) {
            sql.append(" and UPDATE_BY = :updateBy ");
        }
        if (params.get("updateTime") != null && !params.get("updateTime").toString().trim().isEmpty()) {
            sql.append(" and UPDATE_TIME = :updateTime ");
        }
        if (params.get("isDeleted") != null && !params.get("isDeleted").toString().trim().isEmpty()) {
            sql.append(" and IS_DELETED = :isDeleted ");
        }
        if (params.get("revision") != null && !params.get("revision").toString().trim().isEmpty()) {
            sql.append(" and REVISION = :revision ");
        }
        if (params.get("sqlContent") != null && !params.get("sqlContent").toString().trim().isEmpty()) {
            sql.append(" and SQL_CONTENT like :sqlContent ");
            params.put("sqlContent", "%" + params.get("sqlContent") + "%");
        }
        sql.append(" order by  CREATE_TIME desc ");

        org.sagacity.sqltoy.model.Page<SysCommonSearch> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql.toString(), params, SysCommonSearch.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        dataSourceManager.ensureDataSource("db_yzt_config");
        DynamicDataSourceContextHolder.set("db_yzt_config");
        sqlToyLazyDao.executeSql("delete from SYS_COMMON_SEARCH where sql_id = :id", Map.of("id", id));
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
