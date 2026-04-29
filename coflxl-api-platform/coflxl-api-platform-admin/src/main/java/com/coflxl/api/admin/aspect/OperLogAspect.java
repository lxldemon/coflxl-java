package com.coflxl.api.admin.aspect;

import com.coflxl.api.admin.annotation.OperLog;
import com.coflxl.api.common.context.UserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class OperLogAspect {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, OperLog controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperLog controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, OperLog controllerLog, final Exception e, Object jsonResult) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String username = UserContextHolder.getUsername();
            if (username == null || username.trim().isEmpty()) {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    for (Object arg : args) {
                        if (arg instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) arg;
                            if (map.containsKey("username") && map.get("username") != null) {
                                username = map.get("username").toString();
                                break;
                            }
                        }
                    }
                }
            }

            if (username == null || username.trim().isEmpty()) {
                username = "Unknown";
            }

            Map<String, Object> logMap = new HashMap<>();
            logMap.put("title", controllerLog.title());
            logMap.put("businessType", controllerLog.businessType());
            logMap.put("method", joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()");
            logMap.put("requestMethod", request.getMethod());
            logMap.put("operName", username);
            logMap.put("operUrl", request.getRequestURI());
            logMap.put("operIp", request.getRemoteAddr());

            // serialize args
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    java.util.List<Object> safeArgs = new java.util.ArrayList<>();
                    for (Object arg : args) {
                        if (arg != null && !(arg instanceof jakarta.servlet.http.HttpServletRequest)
                                && !(arg instanceof jakarta.servlet.http.HttpServletResponse)
                                && !(arg instanceof org.springframework.web.multipart.MultipartFile)) {
                            safeArgs.add(arg);
                        }
                    }
                    if (safeArgs.isEmpty()) {
                        logMap.put("operParam", request.getQueryString() != null ? request.getQueryString() : "");
                    } else {
                        logMap.put("operParam", objectMapper.writeValueAsString(safeArgs));
                    }
                } catch (Exception ex) {
                    logMap.put("operParam", "Error serialize args: " + ex.getMessage());
                }
            } else {
                logMap.put("operParam", request.getQueryString() != null ? request.getQueryString() : "");
            }

            if (e != null) {
                logMap.put("status", 0);
                logMap.put("errorMsg", e.getMessage());
            } else {
                logMap.put("status", 1);
            }

            if (jsonResult != null) {
                try {
                    logMap.put("jsonResult", objectMapper.writeValueAsString(jsonResult));
                } catch (Exception ex) {
                    logMap.put("jsonResult", jsonResult.toString());
                }
            } else {
                logMap.put("jsonResult", "");
            }

            String sql = "insert into sys_oper_log " +
                    "(title, business_type, method, request_method, oper_name, oper_url, oper_ip, oper_param, json_result, status, error_msg, oper_time) " +
                    "values " +
                    "(:title, :businessType, :method, :requestMethod, :operName, :operUrl, :operIp, :operParam, :jsonResult, :status, :errorMsg, current_timestamp)";

            com.coflxl.api.core.datasource.DynamicDataSourceContextHolder.set("PRIMARY");
            sqlToyLazyDao.executeSql(sql, logMap);
            com.coflxl.api.core.datasource.DynamicDataSourceContextHolder.clear();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
