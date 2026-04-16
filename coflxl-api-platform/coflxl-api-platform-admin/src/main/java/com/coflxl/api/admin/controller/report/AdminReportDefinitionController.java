package com.coflxl.api.admin.controller.report;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ReportDefinition;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/report/template")
public class AdminReportDefinitionController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<ReportDefinition>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        org.sagacity.sqltoy.model.Page<ReportDefinition> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        String sql = "select * from sys_report_definition where 1=1 ";
        if (name != null && !name.isEmpty()) sql += " and name like concat('%', :name, '%') ";
        sql += " order by created_at desc";

        org.sagacity.sqltoy.model.Page<ReportDefinition> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql, Map.of("name", name != null ? name : ""), ReportDefinition.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @GetMapping("/list")
    public ApiResponse<List<ReportDefinition>> list() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        List<ReportDefinition> list = sqlToyLazyDao.findBySql("select * from sys_report_definition order by created_at desc", null, ReportDefinition.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<ReportDefinition> detail(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportDefinition query = new ReportDefinition();
        query.setId(id);
        ReportDefinition def = sqlToyLazyDao.load(query);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(def);
    }

    @PostMapping("/save")
    public ApiResponse<Boolean> save(@RequestBody ReportDefinition reportDefinition) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        if (reportDefinition.getId() == null) {
            reportDefinition.setCreatedAt(LocalDateTime.now());
        }
        reportDefinition.setUpdatedAt(LocalDateTime.now());
        sqlToyLazyDao.saveOrUpdate(reportDefinition);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportDefinition query = new ReportDefinition();
        query.setId(id);
        sqlToyLazyDao.delete(query);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
