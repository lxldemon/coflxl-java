package com.coflxl.api.admin.controller.report;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DataSourceManager;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ReportDefinition;
import com.coflxl.api.core.domain.entity.ReportInstance;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/report/execute")
public class ReportExecutionController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    @PostMapping("/preview")
    public ApiResponse<List<java.util.LinkedHashMap>> preview(@RequestBody Map<String, Object> payload) {
        String sql = (String) payload.get("sql");
        String dataSourceCode = (String) payload.get("dataSourceCode");
        Map<String, Object> params = (Map<String, Object>) payload.get("params");

        if (sql == null || sql.trim().isEmpty()) {
            return ApiResponse.error(400, "SQL cannot be empty");
        }
        if (!sql.trim().toLowerCase().startsWith("select")) {
            return ApiResponse.error(400, "Only SELECT queries are allowed");
        }

        try {
            if (dataSourceCode != null && !dataSourceCode.isEmpty()) {
                dataSourceManager.ensureDataSource(dataSourceCode);
            }
            DynamicDataSourceContextHolder.set(dataSourceCode != null && !dataSourceCode.isEmpty() ? dataSourceCode : "PRIMARY");

            org.sagacity.sqltoy.model.Page<java.util.LinkedHashMap> pageModel = new org.sagacity.sqltoy.model.Page<>();
            pageModel.setPageNo(1L);
            pageModel.setPageSize(50);

            org.sagacity.sqltoy.model.Page<java.util.LinkedHashMap> result = sqlToyLazyDao.findPageBySql(pageModel, sql, params, java.util.LinkedHashMap.class);
            return ApiResponse.success(result.getRows());
        } catch (Exception e) {
            return ApiResponse.error(500, "Execution failed: " + e.getMessage());
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @PostMapping("/instance/{id}")
    public ApiResponse<Map> executeInstance(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> dynamicParams) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ReportInstance queryInstance = new ReportInstance();
        queryInstance.setId(id);
        ReportInstance instance = sqlToyLazyDao.load(queryInstance);
        if (instance == null) {
            DynamicDataSourceContextHolder.clear();
            return ApiResponse.error(404, "Report instance not found");
        }

        if ("OFFLINE".equals(instance.getPublishStatus())) {
            DynamicDataSourceContextHolder.clear();
            return ApiResponse.error(403, "当前报表已下线，无法访问");
        }

        ReportDefinition queryDef = new ReportDefinition();
        queryDef.setId(instance.getDefinitionId());
        ReportDefinition definition = sqlToyLazyDao.load(queryDef);
        if (definition == null) {
            DynamicDataSourceContextHolder.clear();
            return ApiResponse.error(404, "Report definition not found");
        }
        DynamicDataSourceContextHolder.clear();

        String sql = definition.getSqlQuery();
        if (sql == null || sql.trim().isEmpty() || !sql.trim().toLowerCase().startsWith("select")) {
            return ApiResponse.error(400, "Invalid SQL in report definition");
        }

        try {
            Map<String, Object> params = new java.util.HashMap<>();
            if (instance.getActualParametersJson() != null && !instance.getActualParametersJson().isEmpty()) {
                Map<String, Object> actualParams = objectMapper.readValue(instance.getActualParametersJson(), new TypeReference<Map<String, Object>>() {});
                if (actualParams != null) {
                    params.putAll(actualParams);
                }
            }
            if (dynamicParams != null) {
                params.putAll(dynamicParams);
            }

            String dataSourceCode = instance.getDataSourceCode();
            if (dataSourceCode != null && !dataSourceCode.isEmpty()) {
                dataSourceManager.ensureDataSource(dataSourceCode);
            }
            DynamicDataSourceContextHolder.set(dataSourceCode != null && !dataSourceCode.isEmpty() ? dataSourceCode : "PRIMARY");
            List<java.util.LinkedHashMap> data = sqlToyLazyDao.findBySql(sql, params, java.util.LinkedHashMap.class);

            return ApiResponse.success(Map.of(
                    "data", data,
                    "visualizationConfig", definition.getVisualizationConfigJson() != null ? definition.getVisualizationConfigJson() : "{}",
                    "layoutConfig", definition.getLayoutConfigJson() != null ? definition.getLayoutConfigJson() : "{}",
                    "name", instance.getName(),
                    "description", instance.getDescription() != null ? instance.getDescription() : "",
                    "parameters", definition.getParametersJson() != null ? definition.getParametersJson() : "[]"
            ));
        } catch (Exception e) {
            return ApiResponse.error(500, "Execution failed: " + e.getMessage());
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
}
