package com.coflxl.api.core.datasource;

import com.coflxl.api.common.exception.ApiException;
import com.coflxl.api.core.domain.entity.ApiDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataSourceManager {

    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;

    public void ensureDataSource(String dataSourceCode) {
        if ("PRIMARY".equals(dataSourceCode) || dynamicRoutingDataSource.hasDataSource(dataSourceCode)) {
            return;
        }

        DynamicDataSourceContextHolder.set("PRIMARY");
        ApiDataSource dsConfig;
        try {
            dsConfig = sqlToyLazyDao.loadBySql("select * from sys_api_data_source where code = :code", java.util.Map.of("code", dataSourceCode), ApiDataSource.class);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }

        if (dsConfig == null) {
            throw new ApiException(500, "Data source not found: " + dataSourceCode);
        }
        
        if (dsConfig.getJdbcUrl() == null || dsConfig.getJdbcUrl().isEmpty()) {
            throw new ApiException(500, "jdbcUrl is missing for data source: " + dataSourceCode);
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dsConfig.getJdbcUrl());
        dataSource.setUsername(dsConfig.getUsername());
        dataSource.setPassword(dsConfig.getPasswordEncrypted());
        dataSource.setDriverClassName(dsConfig.getDriverClassName());

        dynamicRoutingDataSource.addDataSource(dataSourceCode, dataSource);
    }
}
