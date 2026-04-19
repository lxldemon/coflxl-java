package com.coflxl.api.admin.controller.system;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.SysDept;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sys/dept")
public class AdminDeptController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> getDeptTree() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, parent_id as parentId, name, sort_no as sortNo from sys_dept order by sort_no asc, id asc";
        List<Map> list = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(buildTree(list, null));
    }

    private List<Map<String, Object>> buildTree(List<Map> list, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map map : list) {
            Long currentParentId = map.get("parentId") == null ? null : Long.valueOf(map.get("parentId").toString());
            if ((parentId == null && currentParentId == null) || (parentId != null && parentId.equals(currentParentId))) {
                Map<String, Object> node = new HashMap<>(map);
                Long id = Long.valueOf(map.get("id").toString());
                node.put("children", buildTree(list, id));
                tree.add(node);
            }
        }
        return tree;
    }

    @PostMapping("/save")
    public ApiResponse<Boolean> saveDept(@RequestBody SysDept dept) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        if (dept.getId() == null) {
            dept.setCreatedAt(new java.util.Date());
            dept.setStatus("ACTIVE");
            sqlToyLazyDao.save(dept);
        } else {
            sqlToyLazyDao.update(dept);
        }
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteDept(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        // 检查是否有子部门
        String sql = "select count(1) as cnt from sys_dept where parent_id = :id";
        List<Map> countRes = sqlToyLazyDao.findBySql(sql, Map.of("id", id), Map.class);
        if (countRes != null && !countRes.isEmpty()) {
            Long count = Long.valueOf(countRes.get(0).get("cnt").toString());
            if (count > 0) {
                DynamicDataSourceContextHolder.clear();
                return ApiResponse.error(400, "存在子部门，不允许删除");
            }
        }

        SysDept d = new SysDept();
        d.setId(id);
        sqlToyLazyDao.delete(d);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
