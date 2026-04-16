package com.coflxl.api.admin.controller.api;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/log")
public class AdminCallLogController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/page")
    public ApiResponse<Page<Map<String, Object>>> pageLogs(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "apiCode", required = false) String apiCode) {

        DynamicDataSourceContextHolder.set("PRIMARY");
        Page<Map<String, Object>> pageModel = new Page<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);

        String sql = "select * from sys_api_call_log where 1=1 ";
        if (apiCode != null && !apiCode.isEmpty()) {
            sql += " and api_code = :apiCode ";
        }
        sql += " order by created_at desc";

        Page<Map> result = sqlToyLazyDao.findPageBySql(pageModel, sql, Map.of("apiCode", apiCode == null ? "" : apiCode), Map.class);
        DynamicDataSourceContextHolder.clear();

        return ApiResponse.success((Page) result);
    }
}
