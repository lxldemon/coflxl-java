package com.coflxl.api.core.config;

import org.sagacity.sqltoy.plugins.IUnifyFieldsHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * SqlToy 统一字段赋值处理器
 * 用于在新增和修改时自动为 createdAt 和 updatedAt 赋值
 */
@Component
public class SqlToyUnifyFieldsHandler implements IUnifyFieldsHandler {

    @Override
    public Map<String, Object> createUnifyFields() {
        Map<String, Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        // 这里的 key 必须是实体类中的属性名，而不是数据库字段名
        map.put("createdAt", now);
        map.put("updatedAt", now);
        map.put("createTime", now);
        map.put("updateTime", now);
        return map;
    }

    @Override
    public Map<String, Object> updateUnifyFields() {
        Map<String, Object> map = new HashMap<>();
        map.put("updatedAt", LocalDateTime.now());
        map.put("updateTime", LocalDateTime.now());
        return map;
    }

}
