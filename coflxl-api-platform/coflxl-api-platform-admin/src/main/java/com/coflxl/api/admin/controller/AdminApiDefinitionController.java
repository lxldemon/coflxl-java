package com.coflxl.api.admin.controller;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ApiDefinition;
import com.coflxl.api.core.domain.entity.ApiParamDefinition;
import com.coflxl.api.core.domain.entity.ApiSqlDefinition;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class AdminApiDefinitionController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/list")
    public ApiResponse<List<ApiDefinition>> listApis() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        List<ApiDefinition> list = sqlToyLazyDao.findBySql("select * from sys_api_definition order by created_at desc", null, ApiDefinition.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }

    @GetMapping("/page")
    public ApiResponse<org.sagacity.sqltoy.model.Page<ApiDefinition>> pageApis(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "apiCode", required = false) String apiCode,
            @RequestParam(value = "apiName", required = false) String apiName,
            @RequestParam(value = "systemCode", required = false) String systemCode) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        org.sagacity.sqltoy.model.Page<ApiDefinition> pageModel = new org.sagacity.sqltoy.model.Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        String sql = "select * from sys_api_definition where 1=1 ";
        if (apiCode != null && !apiCode.isEmpty()) sql += " and api_code like concat('%', :apiCode, '%') ";
        if (apiName != null && !apiName.isEmpty()) sql += " and api_name like concat('%', :apiName, '%') ";
        if (systemCode != null && !systemCode.isEmpty()) sql += " and system_code = :systemCode ";
        sql += " order by created_at desc";

        org.sagacity.sqltoy.model.Page<ApiDefinition> result = sqlToyLazyDao.findPageBySql(
                pageModel, sql, java.util.Map.of("apiCode", apiCode == null ? "" : apiCode, "apiName", apiName == null ? "" : apiName, "systemCode", systemCode == null ? "" : systemCode), ApiDefinition.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    @GetMapping("/detail/{apiCode}")
    public ApiResponse<Map<String, Object>> getApiDetail(@PathVariable("apiCode") String apiCode) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        ApiDefinition api = sqlToyLazyDao.loadBySql("select * from sys_api_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode), ApiDefinition.class);
        ApiSqlDefinition sql = sqlToyLazyDao.loadBySql("select * from sys_api_sql_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode), ApiSqlDefinition.class);
        List<ApiParamDefinition> params = sqlToyLazyDao.findBySql("select * from sys_api_param_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode), ApiParamDefinition.class);
        DynamicDataSourceContextHolder.clear();

        Map<String, Object> result = new HashMap<>();
        result.put("api", api);
        result.put("sql", sql);
        result.put("params", params);
        return ApiResponse.success(result);
    }

    @PostMapping("/save")
    @Transactional
    public ApiResponse<Boolean> saveApi(@RequestBody Map<String, Object> payload) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        try {
            // 解析 payload
            Map<String, Object> apiMap = (Map<String, Object>) payload.get("api");
            Map<String, Object> sqlMap = (Map<String, Object>) payload.get("sql");
            List<Map<String, Object>> paramsList = (List<Map<String, Object>>) payload.get("params");

            String apiCode = (String) apiMap.get("apiCode");

            // 保存 API 定义
            ApiDefinition apiDef = new ApiDefinition();
            if (apiMap.get("id") != null) {
                apiDef.setId(Long.valueOf(apiMap.get("id").toString()));
            }
            apiDef.setApiCode(apiCode);
            apiDef.setApiName((String) apiMap.get("apiName"));
            apiDef.setApiPath((String) apiMap.get("apiPath"));
            apiDef.setHttpMethod((String) apiMap.get("httpMethod"));
            apiDef.setOperationType((String) apiMap.get("operationType"));
            apiDef.setExecuteMode((String) apiMap.get("executeMode"));
            apiDef.setDataSourceCode((String) apiMap.get("dataSourceCode"));
            apiDef.setSystemCode((String) apiMap.get("systemCode"));
            apiDef.setStatus("DRAFT"); // 默认草稿
            sqlToyLazyDao.saveOrUpdate(apiDef);

            // 保存 SQL 定义
            ApiSqlDefinition sqlDef = new ApiSqlDefinition();
            if (sqlMap.get("id") != null) {
                sqlDef.setId(Long.valueOf(sqlMap.get("id").toString()));
            }
            sqlDef.setApiCode(apiCode);
            sqlDef.setSqlText((String) sqlMap.get("sqlText"));
            sqlDef.setSqlType((String) apiMap.get("operationType"));
            sqlDef.setEnabledFlag(true);
            sqlToyLazyDao.saveOrUpdate(sqlDef);

            // 保存参数定义
            sqlToyLazyDao.executeSql("delete from sys_api_param_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode));
            if (paramsList != null && !paramsList.isEmpty()) {
                for (Map<String, Object> pMap : paramsList) {
                    ApiParamDefinition pDef = new ApiParamDefinition();
                    pDef.setApiCode(apiCode);
                    pDef.setParamCode((String) pMap.get("paramCode"));
                    pDef.setParamName((String) pMap.get("paramName"));
                    pDef.setDataType((String) pMap.get("dataType"));
                    pDef.setRequiredFlag((Boolean) pMap.get("requiredFlag"));
                    pDef.setDefaultValue((String) pMap.get("defaultValue"));
                    sqlToyLazyDao.save(pDef);
                }
            }
            return ApiResponse.success(true);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @PostMapping("/publish/{apiCode}")
    public ApiResponse<Boolean> publishApi(@PathVariable("apiCode") String apiCode) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("update sys_api_definition set status = 'PUBLISHED' where api_code = :apiCode", java.util.Map.of("apiCode", apiCode));
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/offline/{apiCode}")
    public ApiResponse<Boolean> offlineApi(@PathVariable("apiCode") String apiCode) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("update sys_api_definition set status = 'OFFLINE' where api_code = :apiCode", java.util.Map.of("apiCode", apiCode));
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
