package com.coflxl.api.admin.controller;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ReportInstance;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/report/instance")
public class AdminReportInstanceController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<ReportInstance>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        org.sagacity.sqltoy.model.Page<ReportInstance> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        String sql = "select * from sys_report_instance where 1=1 ";
        if (name != null && !name.isEmpty()) sql += " and name like concat('%', :name, '%') ";
        sql += " order by created_at desc";

        org.sagacity.sqltoy.model.Page<ReportInstance> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql, Map.of("name", name != null ? name : ""), ReportInstance.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<ReportInstance> detail(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportInstance query = new ReportInstance();
        query.setId(id);
        ReportInstance instance = sqlToyLazyDao.load(query);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(instance);
    }

    @PostMapping("/save")
    public ApiResponse<Boolean> save(@RequestBody ReportInstance reportInstance) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        if (reportInstance.getId() == null) {
            reportInstance.setCreatedAt(LocalDateTime.now());
            reportInstance.setPublishStatus("DRAFT");
        }
        reportInstance.setUpdatedAt(LocalDateTime.now());
        sqlToyLazyDao.saveOrUpdate(reportInstance);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportInstance query = new ReportInstance();
        query.setId(id);
        sqlToyLazyDao.delete(query);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/publish/{id}")
    public ApiResponse<Boolean> publish(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportInstance query = new ReportInstance();
        query.setId(id);
        ReportInstance instance = sqlToyLazyDao.load(query);
        if (instance != null) {
            instance.setPublishStatus("PUBLISHED");
            instance.setUpdatedAt(LocalDateTime.now());
            sqlToyLazyDao.update(instance);
        }
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/offline/{id}")
    public ApiResponse<Boolean> offline(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportInstance query = new ReportInstance();
        query.setId(id);
        ReportInstance instance = sqlToyLazyDao.load(query);
        if (instance != null) {
            instance.setPublishStatus("OFFLINE");
            instance.setUpdatedAt(LocalDateTime.now());
            sqlToyLazyDao.update(instance);
        }
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
