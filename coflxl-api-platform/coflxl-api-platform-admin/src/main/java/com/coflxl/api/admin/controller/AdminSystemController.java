package com.coflxl.api.admin.controller;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ApiSystem;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/system")
public class AdminSystemController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @PostMapping("/save")
    public ApiResponse<Boolean> saveSystem(@RequestBody ApiSystem system) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.saveOrUpdate(system);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/list")
    public ApiResponse<List<ApiSystem>> listSystems() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        List<ApiSystem> list = sqlToyLazyDao.findBySql("select * from sys_api_system", null, ApiSystem.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<ApiSystem>> pageSystems(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "systemCode", required = false) String systemCode,
            @RequestParam(value = "systemName", required = false) String systemName) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        org.sagacity.sqltoy.model.Page<ApiSystem> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        String sql = "select * from sys_api_system where 1=1 ";
        if (systemCode != null && !systemCode.isEmpty()) sql += " and system_code like concat('%', :systemCode, '%') ";
        if (systemName != null && !systemName.isEmpty()) sql += " and system_name like concat('%', :systemName, '%') ";

        org.sagacity.sqltoy.model.Page<ApiSystem> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql, java.util.Map.of("systemCode", systemCode == null ? "" : systemCode, "systemName", systemName == null ? "" : systemName), ApiSystem.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @PostMapping("/delete/{systemCode}")
    public ApiResponse<Boolean> deleteSystem(@PathVariable("systemCode") String systemCode) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("delete from sys_api_system where system_code = :systemCode", java.util.Map.of("systemCode", systemCode));
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
