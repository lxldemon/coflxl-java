package com.coflxl.api.gateway.controller;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.service.ApiExecuteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/open")
public class OpenApiExecuteController {

    @Autowired
    private ApiExecuteService apiExecuteService;

    @RequestMapping(value = "/{apiCode}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ApiResponse<Object> executeApi(
            @PathVariable String apiCode,
            @RequestParam(required = false) Map<String, Object> queryParams,
            @RequestBody(required = false) Map<String, Object> bodyParams,
            @RequestHeader(required = false) Map<String, String> headers,
            HttpServletRequest request) {

        // 核心执行逻辑已下沉至 ApiExecuteService，包含以下步骤：
        // 1. 解析 apiCode
        // 2. 查询接口定义与已发布版本
        // 3. 校验接口状态
        // 4. 校验调用方权限
        // 5. 加载参数定义并执行参数校验 / 类型转换
        // 6. 选择数据源
        // 7. 进行 SQL / Adapter 执行 (支持 QUERY, PAGE, INSERT, UPDATE, DELETE)
        // 8. 对结果进行统一封装
        // 9. 记录调用日志

        Object result = apiExecuteService.execute(apiCode, queryParams, bodyParams, request);
        return ApiResponse.success(result);
    }
}
