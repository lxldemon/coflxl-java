package com.coflxl.api.admin.controller.auth;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.common.utils.JwtUtil;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> loginParam) {
        String username = loginParam.get("username");
        String password = loginParam.get("password");

        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, username, nickname, status from sys_user where username = :username and password = :password";
        List<Map> users = sqlToyLazyDao.findBySql(sql, Map.of("username", username, "password", password), Map.class);
        DynamicDataSourceContextHolder.clear();

        if (users == null || users.isEmpty()) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        Map user = users.get(0);
        if (!"ACTIVE".equals(user.get("status"))) {
            return ApiResponse.error(401, "用户已被禁用");
        }

        Long userId = Long.valueOf(user.get("id").toString());
        String token = JwtUtil.createToken(userId, username);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);

        return ApiResponse.success(result);
    }

    @GetMapping("/userInfo")
    public ApiResponse<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token != null && (token.startsWith("Bearer ") || token.startsWith("bearer "))) {
            token = token.substring(7);
        }
        var jwt = JwtUtil.verifyToken(token);
        if (jwt == null) {
            return ApiResponse.error(401, "未登录或登录已过期");
        }

        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, username, nickname, status from sys_user where id = :id";
        List<Map> users = sqlToyLazyDao.findBySql(sql, Map.of("id", jwt.getClaim("userId").asLong()), Map.class);
        DynamicDataSourceContextHolder.clear();

        if (users == null || users.isEmpty()) {
            return ApiResponse.error(401, "用户不存在");
        }

        Map user = users.get(0);
        // return mock menu and permissions for now to make UI dynamic
        Map<String, Object> userInfo = new HashMap<>(user);
        userInfo.put("roles", List.of("ADMIN"));
        return ApiResponse.success(userInfo);
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success(true);
    }
}

