package com.coflxl.api.admin.controller.system;

import com.coflxl.api.admin.annotation.OperLog;
import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/admin/sys/operLog")
public class OperLogController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/page")
    public ApiResponse<Page<Map>> page(
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName) {

        Page<Map> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);

        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, title, business_type as businessType, method, request_method as requestMethod, " +
                "oper_name as operName, oper_url as operUrl, oper_ip as operIp, oper_param as operParam, json_result as jsonResult, error_msg as errorMsg, status, oper_time as operTime " +
                "from sys_oper_log " +
                "where 1=1 " +
                "#[and title like :title] " +
                "#[and oper_name like :operName] " +
                "order by id desc";

        Map<String, Object> params = new HashMap<>();
        params.put("title", StringUtils.hasText(title) ? "%" + title + "%" : null);
        params.put("operName", StringUtils.hasText(operName) ? "%" + operName + "%" : null);

        page = sqlToyLazyDao.findPageBySql(page, sql, params, Map.class);
        DynamicDataSourceContextHolder.clear();

        return ApiResponse.success(page);
    }
}
