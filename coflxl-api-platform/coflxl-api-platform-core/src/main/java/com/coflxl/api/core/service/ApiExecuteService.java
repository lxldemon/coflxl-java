package com.coflxl.api.core.service;

import com.coflxl.api.common.enums.ErrorCodeEnum;
import com.coflxl.api.common.exception.ApiException;
import com.coflxl.api.core.datasource.DataSourceManager;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.datasource.DynamicRoutingDataSource;
import com.coflxl.api.core.domain.entity.ApiDefinition;
import com.coflxl.api.core.domain.entity.ApiParamDefinition;
import com.coflxl.api.core.domain.entity.ApiSqlDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiExecuteService {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @Autowired
    private DataSourceManager dataSourceManager;
    
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object execute(String apiCode, Map<String, Object> queryParams, Map<String, Object> bodyParams, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;
        Map<String, Object> mergedParams = new HashMap<>();

        try {
            // 1. 解析 apiCode & 2. 查询接口定义与已发布版本
            DynamicDataSourceContextHolder.set("PRIMARY");
            ApiDefinition apiDef = sqlToyLazyDao.loadBySql("select * from sys_api_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode), ApiDefinition.class);
            if (apiDef == null) {
                throw new ApiException(ErrorCodeEnum.API_NOT_FOUND);
            }

            // 3. 校验接口状态
            if (!"PUBLISHED".equals(apiDef.getStatus())) {
                throw new ApiException(ErrorCodeEnum.API_OFFLINE);
            }

            // 4. 校验调用方权限 (TODO: 接入鉴权中心，此处预留扩展点)
            // checkPermission(apiDef, request);

            // 合并参数
            if (queryParams != null) mergedParams.putAll(queryParams);
            if (bodyParams != null) mergedParams.putAll(bodyParams);

            // 5. 加载参数定义并执行参数校验 / 类型转换
            List<ApiParamDefinition> paramDefs = sqlToyLazyDao.findBySql("select * from sys_api_param_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode), ApiParamDefinition.class);
            for (ApiParamDefinition pDef : paramDefs) {
                Object val = mergedParams.get(pDef.getParamCode());
                if (val == null && pDef.getDefaultValue() != null) {
                    val = pDef.getDefaultValue();
                    mergedParams.put(pDef.getParamCode(), val);
                }
                if (Boolean.TRUE.equals(pDef.getRequiredFlag()) && val == null) {
                    throw new ApiException(ErrorCodeEnum.PARAM_ERROR.getCode(), "Missing required parameter: " + pDef.getParamCode());
                }
            }

            ApiSqlDefinition sqlDef = sqlToyLazyDao.loadBySql("select * from sys_api_sql_definition where api_code = :apiCode", java.util.Map.of("apiCode", apiCode), ApiSqlDefinition.class);
            if (sqlDef == null) {
                throw new ApiException(500, "SQL definition not found for API: " + apiCode);
            }

            // 6. 选择数据源
            String dsCode = apiDef.getDataSourceCode();
            if (dsCode != null && !dsCode.isEmpty() && !"PRIMARY".equals(dsCode)) {
                dataSourceManager.ensureDataSource(dsCode);
                DynamicDataSourceContextHolder.set(dsCode);
            }

            // 7. 进行 SQL / Adapter 执行 & 8. 对结果进行统一封装
            // 使用编程式事务，确保在动态切换的数据源上开启事务
            DataSourceTransactionManager txManager = new DataSourceTransactionManager(dynamicRoutingDataSource);
            TransactionTemplate txTemplate = new TransactionTemplate(txManager);

            Object result = txTemplate.execute(status -> {
                try {
                    String opType = apiDef.getOperationType();
                    String execMode = apiDef.getExecuteMode();

                    if ("MULTI_SQL".equalsIgnoreCase(execMode)) {
                        // 主子表/多SQL复合执行
                        return executeMultiSql(sqlDef.getSqlText(), mergedParams);
                    } else if (opType != null && opType.startsWith("BATCH_")) {
                        // 批量增删改
                        return executeBatchSql(sqlDef.getSqlText(), mergedParams);
                    } else if ("PAGE".equalsIgnoreCase(opType)) {
                        // 分页查询
                        Page<Map<String, Object>> pageModel = new Page<>();
                        pageModel.setPageNo(Long.parseLong(String.valueOf(mergedParams.getOrDefault("pageNo", "1"))));
                        pageModel.setPageSize(Integer.parseInt(String.valueOf(mergedParams.getOrDefault("pageSize", "10"))));
                        return sqlToyLazyDao.findPageBySql(pageModel, sqlDef.getSqlText(), mergedParams, Map.class);
                    } else if ("INSERT".equalsIgnoreCase(opType) || "UPDATE".equalsIgnoreCase(opType) || "DELETE".equalsIgnoreCase(opType)) {
                        // 单条增删改
                        Long affectedRows = sqlToyLazyDao.executeSql(sqlDef.getSqlText(), mergedParams);
                        Map<String, Object> resMap = new HashMap<>();
                        resMap.put("affectedRows", affectedRows);
                        return resMap;
                    } else {
                        // 默认普通查询 (QUERY)
                        return sqlToyLazyDao.findBySql(sqlDef.getSqlText(), mergedParams, Map.class);
                    }
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw new RuntimeException(e);
                }
            });

            return result;

        } catch (Exception e) {
            success = false;
            Throwable cause = e.getCause();
            if (e instanceof RuntimeException && cause instanceof ApiException) {
                errorMessage = cause.getMessage();
                throw (ApiException) cause;
            }
            errorMessage = e.getMessage();
            throw (e instanceof ApiException) ? (ApiException) e : new ApiException(500, errorMessage);
        } finally {
            long costMs = System.currentTimeMillis() - startTime;
            // 9. 记录调用日志
            recordCallLog(apiCode, mergedParams, success, errorMessage, costMs, request);
            DynamicDataSourceContextHolder.clear();
        }
    }

    /**
     * 执行批量 SQL (如批量插入、批量更新)
     */
    @SuppressWarnings("unchecked")
    private Object executeBatchSql(String sql, Map<String, Object> params) {
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) params.get("dataList");
        if (dataList == null || dataList.isEmpty()) {
            throw new ApiException(ErrorCodeEnum.PARAM_ERROR.getCode(), "Batch operation requires 'dataList' parameter array");
        }
        long totalAffected = 0;
        for (Map<String, Object> item : dataList) {
            Map<String, Object> itemParams = new HashMap<>(params);
            itemParams.putAll(item); // 每一行的参数覆盖全局参数
            totalAffected += sqlToyLazyDao.executeSql(sql, itemParams);
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("affectedRows", totalAffected);
        return resMap;
    }

    /**
     * 执行多条 SQL (主子表复合场景)
     */
    @SuppressWarnings("unchecked")
    private Object executeMultiSql(String sqlTextJson, Map<String, Object> params) throws Exception {
        List<Map<String, Object>> steps = objectMapper.readValue(sqlTextJson, new TypeReference<List<Map<String, Object>>>(){});
        long totalAffected = 0;
        
        for (Map<String, Object> step : steps) {
            String type = (String) step.get("type");
            String sql = (String) step.get("sql");
            
            if ("BATCH".equalsIgnoreCase(type)) {
                String loopVar = (String) step.get("loopVar");
                List<Map<String, Object>> list = (List<Map<String, Object>>) params.get(loopVar);
                if (list != null) {
                    for (Map<String, Object> item : list) {
                        Map<String, Object> itemParams = new HashMap<>(params);
                        // 将子表对象的属性加上前缀放入参数中，避免与主表字段冲突
                        // 例如 loopVar="item", key="id" -> "item_id"
                        for (Map.Entry<String, Object> entry : item.entrySet()) {
                            itemParams.put(loopVar + "_" + entry.getKey(), entry.getValue());
                        }
                        totalAffected += sqlToyLazyDao.executeSql(sql, itemParams);
                    }
                }
            } else {
                // SINGLE 单条执行 (通常是主表)
                totalAffected += sqlToyLazyDao.executeSql(sql, params);
            }
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("affectedRows", totalAffected);
        return resMap;
    }

    private void recordCallLog(String apiCode, Map<String, Object> params, boolean success, String errorMessage, long costMs, HttpServletRequest request) {
        try {
            DynamicDataSourceContextHolder.set("PRIMARY");
            String paramsJson = objectMapper.writeValueAsString(params);
            String ip = request != null ? request.getRemoteAddr() : "unknown";
            
            String insertLogSql = "insert into sys_api_call_log(api_code, request_params, response_code, response_message, cost_ms, success_flag, client_ip) " +
                                  "values(:apiCode, :params, :code, :msg, :cost, :success, :ip)";
            
            Map<String, Object> logParams = new HashMap<>();
            logParams.put("apiCode", apiCode);
            logParams.put("params", paramsJson);
            logParams.put("code", success ? 200 : 500);
            logParams.put("msg", errorMessage);
            logParams.put("cost", costMs);
            logParams.put("success", success);
            logParams.put("ip", ip);
            
            sqlToyLazyDao.executeSql(insertLogSql, logParams);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            e.printStackTrace();
        }
    }
}
