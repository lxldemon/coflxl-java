package com.coflxl.api.admin.controller;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ApiDataSource;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/data-source")
public class AdminDataSourceController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @PostMapping("/save")
    public ApiResponse<Boolean> saveDataSource(@RequestBody ApiDataSource dataSource) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.saveOrUpdate(dataSource);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/list")
    public ApiResponse<List<ApiDataSource>> listDataSources() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        List<ApiDataSource> list = sqlToyLazyDao.findBySql("select * from sys_api_data_source", null, ApiDataSource.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<ApiDataSource>> pageDataSources(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "systemCode", required = false) String systemCode) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        org.sagacity.sqltoy.model.Page<ApiDataSource> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        String sql = "select * from sys_api_data_source where 1=1 ";
        if (code != null && !code.isEmpty()) sql += " and code like concat('%', :code, '%') ";
        if (name != null && !name.isEmpty()) sql += " and name like concat('%', :name, '%') ";
        if (systemCode != null && !systemCode.isEmpty()) sql += " and system_code = :systemCode ";

        org.sagacity.sqltoy.model.Page<ApiDataSource> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql, java.util.Map.of("code", code == null ? "" : code, "name", name == null ? "" : name, "systemCode", systemCode == null ? "" : systemCode), ApiDataSource.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @PostMapping("/delete/{code}")
    public ApiResponse<Boolean> deleteDataSource(@PathVariable("code") String code) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("delete from sys_api_data_source where code = :code", java.util.Map.of("code", code));
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
