package com.coflxl.api.admin.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.common.utils.JwtUtil;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 1. Check whitelist
        DynamicDataSourceContextHolder.set("PRIMARY");
        String whitelistSql = "select url_pattern from sys_api_whitelist";
        List<Map> whitelist = sqlToyLazyDao.findBySql(whitelistSql, null, Map.class);
        DynamicDataSourceContextHolder.clear();

        if (whitelist != null) {
            for (Map map : whitelist) {
                String pattern = (String) map.get("urlPattern");
                if (pattern != null && pathMatcher.match(pattern, requestURI)) {
                    return true;
                }
            }
        }

        // Default openapi whitelist
        if (requestURI.startsWith("/api/open/")) {
            return true;
        }

        // 2. Validate token
        String token = request.getHeader("Authorization");
        if (token != null && (token.startsWith("Bearer ") || token.startsWith("bearer "))) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            returnError(response, 401, "未登录，缺少Token");
            return false;
        }

        DecodedJWT jwt = JwtUtil.verifyToken(token);
        if (jwt == null) {
            returnError(response, 401, "登录已过期或Token无效");
            return false;
        }

        // Put user info into request attribute for later use if needed
        request.setAttribute("userId", jwt.getClaim("userId").asLong());
        String username = jwt.getClaim("username").asString();
        request.setAttribute("username", username);
        com.coflxl.api.common.context.UserContextHolder.setUsername(username);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        com.coflxl.api.common.context.UserContextHolder.clear();
    }

    private void returnError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200); // return 200 technically but custom code inside
        ApiResponse<Void> apiResponse = ApiResponse.error(code, msg);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
