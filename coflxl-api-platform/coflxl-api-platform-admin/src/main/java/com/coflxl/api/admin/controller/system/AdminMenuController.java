package com.coflxl.api.admin.controller.system;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.SysMenu;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/sys/menu")
public class AdminMenuController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, parent_id as parentId, name, path, component, icon, sort_no as sortNo, visible_flag as visibleFlag, keep_alive_flag as keepAliveFlag from sys_menu order by sort_no asc";
        List<Map> allMenus = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();

        return ApiResponse.success(buildTree(allMenus, null));
    }

    @GetMapping("/userMenus")
    public ApiResponse<List<Map<String, Object>>> userMenus(@RequestAttribute("userId") Long userId) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select distinct m.id, m.parent_id as parentId, m.name, m.path, m.component, m.icon, m.sort_no as sortNo, m.visible_flag as visibleFlag, m.keep_alive_flag as keepAliveFlag " +
                "from sys_menu m " +
                "join sys_role_menu rm on m.id = rm.menu_id " +
                "join sys_user_role ur on rm.role_id = ur.role_id " +
                "where ur.user_id = :userId and m.visible_flag = true " +
                "order by m.sort_no asc";
        List<Map> userMenus = sqlToyLazyDao.findBySql(sql, Map.of("userId", userId), Map.class);
        DynamicDataSourceContextHolder.clear();

        return ApiResponse.success(buildTree(userMenus, null));
    }

    private List<Map<String, Object>> buildTree(List<Map> allNodes, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map node : allNodes) {
            Long curParentId = node.get("parentId") == null ? null : Long.valueOf(node.get("parentId").toString());
            if ((parentId == null && curParentId == null) || (parentId != null && parentId.equals(curParentId))) {
                Map<String, Object> menuNode = (Map<String, Object>) node;
                Long curId = Long.valueOf(node.get("id").toString());
                List<Map<String, Object>> children = buildTree(allNodes, curId);
                if (!children.isEmpty()) {
                    menuNode.put("children", children);
                }
                tree.add(menuNode);
            }
        }
        return tree;
    }

    @PostMapping("/save")
    public ApiResponse<Boolean> saveMenu(@RequestBody SysMenu menu) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        sqlToyLazyDao.saveOrUpdate(menu);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteMenu(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("id", id);
        sqlToyLazyDao.executeSql("delete from sys_role_menu where menu_id = :id", params);
        SysMenu menu = new SysMenu();
        menu.setId(id);
        sqlToyLazyDao.delete(menu);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }
}
