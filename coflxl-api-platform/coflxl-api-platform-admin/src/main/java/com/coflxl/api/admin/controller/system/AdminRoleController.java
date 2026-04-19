package com.coflxl.api.admin.controller.system;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.SysRole;
import com.coflxl.api.core.domain.entity.SysUser;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sys/role")
public class AdminRoleController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/page")
    public ApiResponse<Page<SysRole>> page(
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name
    ) {
        org.sagacity.sqltoy.model.Page<SysRole> page = new org.sagacity.sqltoy.model.Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, code, name, status from sys_role where 1=1 #[and name like :name] order by id desc";
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("name",
                StringUtils.hasText(name) ? "%" + name.trim() + "%" : null
        );
        page = sqlToyLazyDao.findPageBySql(page, sql, params, SysRole.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(page);
    }

    @PostMapping("/save1")
    public ApiResponse<Boolean> saveRole(@RequestBody SysRole role) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.saveOrUpdate(role);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }@PostMapping("/save")
    public ApiResponse<Boolean> saveRole1(@RequestBody SysRole role) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.saveOrUpdate(role);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteRole(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("id", id);
        sqlToyLazyDao.executeSql("delete from sys_role_menu where role_id = :id", params);
        sqlToyLazyDao.executeSql("delete from sys_user_role where role_id = :id", params);
        SysRole role = new SysRole();
        role.setId(id);
        sqlToyLazyDao.delete(role);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/list")
    public ApiResponse<List<Map>> list() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, code, name from sys_role where status = 'ACTIVE'";
        List<Map> list = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }

    @PostMapping("/assignMenus")
    public ApiResponse<Boolean> assignMenus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        List<Integer> menuIds = (List<Integer>) params.get("menuIds");

        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("delete from sys_role_menu where role_id = :roleId", Map.of("roleId", roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Integer menuId : menuIds) {
                sqlToyLazyDao.executeSql("insert into sys_role_menu (role_id, menu_id) values (:roleId, :menuId)",
                        Map.of("roleId", roleId, "menuId", menuId));
            }
        }
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/menus")
    public ApiResponse<List<Map>> getRoleMenus(@RequestParam Long roleId) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select menu_id as menuId from sys_role_menu where role_id = :roleId";
        List<Map> list = sqlToyLazyDao.findBySql(sql, Map.of("roleId", roleId), Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }
}
