package com.coflxl.api.admin.controller.system;

import com.coflxl.api.admin.annotation.OperLog;
import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.ApiDataSource;
import com.coflxl.api.core.domain.entity.SysUser;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sys/user")
public class AdminUserController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/page")
    public ApiResponse<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long deptId
    ) {
        org.sagacity.sqltoy.model.Page<SysUser> page = new org.sagacity.sqltoy.model.Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, username, nickname, status, dept_id as deptId, created_at as createdAt from sys_user where 1=1 #[and username like :username] #[and dept_id = :deptId] order by id desc";
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("username",
                StringUtils.hasText(username) ? "%" + username.trim() + "%" : null
        );
        params.put("deptId", deptId);
        page = sqlToyLazyDao.findPageBySql(page, sql, params, SysUser.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(page);
    }

    @OperLog(title = "用户管理", businessType = "SAVE")
    @PostMapping("/save")
    public ApiResponse<Boolean> saveUser(@RequestBody SysUser user) {
        DynamicDataSourceContextHolder.set("PRIMARY");

        if (user.getId() == null) {
            String rawPassword = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
            user.setPassword(org.springframework.util.DigestUtils.md5DigestAsHex(rawPassword.getBytes()));
            sqlToyLazyDao.save(user);
        } else {
            SysUser user1 = new SysUser();
            user1.setId(user.getId());
            SysUser dbUser = sqlToyLazyDao.load(user1);
            if (dbUser != null) {
                if (!StringUtils.hasText(user.getPassword())) {
                    // 如果为空，不更新密码，仅更新其他字段
                    user.setPassword(dbUser.getPassword());
                } else if (!user.getPassword().equals(dbUser.getPassword())) {
                    // 如果传入了新密码，且跟数据库不一致（未加密的），则加密。为简单这里直接重新计算md5
                    user.setPassword(org.springframework.util.DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
                }
                user.setCreatedAt(dbUser.getCreatedAt()); // 保持原创建时间
            }
            sqlToyLazyDao.update(user);
        }

        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @OperLog(title = "用户管理", businessType = "DELETE")
    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteUser(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("id", id);
        sqlToyLazyDao.executeSql("delete from sys_user_role where user_id = :id", params);
        SysUser user1 = new SysUser();
        user1.setId(id);
        sqlToyLazyDao.delete(user1);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/assignRoles")
    public ApiResponse<Boolean> assignRoles(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        List<Integer> roleIds = (List<Integer>) params.get("roleIds");

        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.executeSql("delete from sys_user_role where user_id = :userId", Map.of("userId", userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Integer roleId : roleIds) {
                sqlToyLazyDao.executeSql("insert into sys_user_role (user_id, role_id) values (:userId, :roleId)",
                        Map.of("userId", userId, "roleId", roleId));
            }
        }
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map>> getUserRoles(@RequestParam Long userId) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select role_id as roleId from sys_user_role where user_id = :userId";
        List<Map> list = sqlToyLazyDao.findBySql(sql, Map.of("userId", userId), Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(list);
    }
}
