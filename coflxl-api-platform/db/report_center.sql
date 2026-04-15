-- 报表模板定义表
CREATE TABLE `sys_report_definition` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`name` varchar(100) NOT NULL COMMENT '模板名称',
`description` varchar(255) DEFAULT NULL COMMENT '描述',
`category` varchar(50) DEFAULT NULL COMMENT '分类',
`data_source_code` varchar(50) DEFAULT NULL COMMENT '分类',
`sql_query` text NOT NULL COMMENT 'SQL查询语句',
`parameters_json` text DEFAULT NULL COMMENT '参数定义JSON',
`visualization_config_json` text DEFAULT NULL COMMENT '可视化配置JSON',
`layout_config_json` text DEFAULT NULL COMMENT '布局配置JSON',
`status` varchar(20) DEFAULT NULL COMMENT '状态',
`created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表模板定义表';

-- 报表实例表
CREATE TABLE `sys_report_instance` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`definition_id` bigint(20) NOT NULL COMMENT '模板ID',
`name` varchar(100) NOT NULL COMMENT '实例名称',
`description` varchar(255) DEFAULT NULL COMMENT '描述',
`data_source_code` varchar(50) NOT NULL COMMENT '数据源编码',
`actual_parameters_json` text DEFAULT NULL COMMENT '实际参数JSON',
`publish_status` varchar(20) DEFAULT 'DRAFT' COMMENT '发布状态(DRAFT,PUBLISHED,OFFLINE)',
`access_url` varchar(255) DEFAULT NULL COMMENT '访问URL',
`visibility` varchar(20) DEFAULT NULL COMMENT '可见性',
`created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表实例表';