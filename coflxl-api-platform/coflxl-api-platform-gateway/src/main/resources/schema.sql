DROP TABLE IF EXISTS sys_api_data_source;
CREATE TABLE sys_api_data_source (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
code VARCHAR(64) NOT NULL,
name VARCHAR(128) NOT NULL,
db_type VARCHAR(32),
jdbc_url VARCHAR(512),
username VARCHAR(64),
password_encrypted VARCHAR(256),
driver_class_name VARCHAR(128),
read_only_flag BOOLEAN,
pool_config_json TEXT,
system_code VARCHAR(64),
status VARCHAR(32),
remark VARCHAR(256),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS sys_api_definition;
CREATE TABLE sys_api_definition (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
api_code VARCHAR(64) NOT NULL,
api_name VARCHAR(128) NOT NULL,
api_path VARCHAR(256) NOT NULL,
http_method VARCHAR(16),
operation_type VARCHAR(32),
data_source_code VARCHAR(64),
system_code VARCHAR(64),
execute_mode VARCHAR(32),
status VARCHAR(32),
auth_type VARCHAR(32),
timeout_ms INT,
rate_limit_per_minute INT,
cache_enabled BOOLEAN,
cache_ttl_seconds INT,
current_version VARCHAR(32),
remark VARCHAR(256),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS sys_api_sql_definition;
CREATE TABLE sys_api_sql_definition (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
api_code VARCHAR(64) NOT NULL,
version VARCHAR(32),
sql_text TEXT,
sql_type VARCHAR(32),
result_mapping_json TEXT,
safe_level INT,
enabled_flag BOOLEAN,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS sys_api_param_definition;
CREATE TABLE sys_api_param_definition (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
api_code VARCHAR(64) NOT NULL,
param_code VARCHAR(64) NOT NULL,
param_name VARCHAR(128),
source_type VARCHAR(32),
data_type VARCHAR(32),
required_flag BOOLEAN,
default_value VARCHAR(256),
regex_rule VARCHAR(256),
sort_no INT
);

DROP TABLE IF EXISTS sys_api_call_log;
CREATE TABLE sys_api_call_log (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
api_code VARCHAR(64),
request_params TEXT,
response_code INT,
response_message TEXT,
cost_ms BIGINT,
success_flag BOOLEAN,
client_ip VARCHAR(64),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS sys_api_system;
CREATE TABLE sys_api_system (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
system_code VARCHAR(64) NOT NULL UNIQUE,
system_name VARCHAR(128) NOT NULL,
dev_engineer VARCHAR(64),
ss_engineer VARCHAR(64),
remark VARCHAR(256),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入目标数据源配置 (指向另一个内存数据库 targetdb)
INSERT INTO sys_api_data_source (code, name, db_type, jdbc_url, username, password_encrypted, driver_class_name)
VALUES ('target_db', 'Test Target DB', 'H2', 'jdbc:h2:mem:targetdb;INIT=RUNSCRIPT FROM ''classpath:target_schema.sql'';DB_CLOSE_DELAY=-1', 'sa', '', 'org.h2.Driver');

-- 1. 查询列表接口 (QUERY)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, data_source_code, status)
VALUES ('getUserList', '获取用户列表', '/getUserList', 'GET', 'QUERY', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('getUserList', 'select * from target_user where 1=1 #[and age >= :minAge]', 'SELECT', true);

-- 2. 分页查询接口 (PAGE)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, data_source_code, status)
VALUES ('getUserPage', '分页获取用户', '/getUserPage', 'GET', 'PAGE', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('getUserPage', 'select * from target_user where 1=1 #[and name like :name]', 'SELECT', true);
INSERT INTO sys_api_param_definition (api_code, param_code, param_name, required_flag, default_value)
VALUES ('getUserPage', 'pageNo', '页码', false, '1'), ('getUserPage', 'pageSize', '每页条数', false, '10');

-- 3. 新增接口 (INSERT)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, data_source_code, status)
VALUES ('addUser', '新增用户', '/addUser', 'POST', 'INSERT', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('addUser', 'insert into target_user(id, name, age) values(:id, :name, :age)', 'INSERT', true);
INSERT INTO sys_api_param_definition (api_code, param_code, required_flag) VALUES ('addUser', 'id', true), ('addUser', 'name', true), ('addUser', 'age', true);

-- 4. 修改接口 (UPDATE)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, data_source_code, status)
VALUES ('updateUser', '修改用户', '/updateUser', 'POST', 'UPDATE', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('updateUser', 'update target_user set name=:name, age=:age where id=:id', 'UPDATE', true);
INSERT INTO sys_api_param_definition (api_code, param_code, required_flag) VALUES ('updateUser', 'id', true);

-- 5. 删除接口 (DELETE)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, data_source_code, status)
VALUES ('deleteUser', '删除用户', '/deleteUser', 'POST', 'DELETE', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('deleteUser', 'delete from target_user where id=:id', 'DELETE', true);
INSERT INTO sys_api_param_definition (api_code, param_code, required_flag) VALUES ('deleteUser', 'id', true);

-- 6. 批量新增接口 (BATCH_INSERT)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, data_source_code, status)
VALUES ('batchAddUser', '批量新增用户', '/batchAddUser', 'POST', 'BATCH_INSERT', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('batchAddUser', 'insert into target_user(id, name, age) values(:id, :name, :age)', 'INSERT', true);

-- 7. 主子表复合写入接口 (MULTI_SQL 事务)
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, execute_mode, data_source_code, status)
VALUES ('createOrder', '创建订单(主子表)', '/createOrder', 'POST', 'INSERT', 'MULTI_SQL', 'target_db', 'PUBLISHED');
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag)
VALUES ('createOrder', '[
{"type": "SINGLE", "sql": "insert into target_order(id, order_no, total_amount) values(:id, :orderNo, :totalAmount)"},
{"type": "BATCH", "loopVar": "item", "sql": "insert into target_order_item(id, order_id, item_name, price) values(:item_id, :id, :item_itemName, :item_price)"}
]', 'MULTI', true);

-- 迁移系统配置
INSERT INTO sys_api_system (system_code, system_name, dev_engineer, ss_engineer) VALUES
('system_csxt', '财审项目', '占贤银', '李秀'),
('system_guanwang', '本地Mysql测试', '刘小龙', '刘小龙'),
('system_jhyh', '计划养护系统', '刘小龙', '李秀'),
('system_sjzl', '数据治理与决策分析', '刘小龙', '李秀'),
('system_yzt', '一张图', '刘小龙', '刘小龙');

-- 迁移数据源配置
INSERT INTO sys_api_data_source (code, name, jdbc_url, username, password_encrypted, driver_class_name, status, system_code) VALUES
('db_127_mysql', '本地Mysql数据库(guanwang)', 'jdbc:mysql://127.0.0.1:3306/guanwang', 'root', '123456', 'com.mysql.cj.jdbc.Driver', 'ACTIVE', 'system_guanwang'),
('db_csxt', '财审系统数据配置', 'jdbc:oracle:thin:@36.2.6.16:1521:orcl', 'JXCSXM', 'JXCSXM', 'oracle.jdbc.driver.OracleDriver', 'ACTIVE', 'system_csxt'),
('db_jhyh', '计划养护系统数据配置', 'jdbc:oracle:thin:@36.2.6.16:1521:orcl', 'JXJHYH', 'JXJHYH', 'oracle.jdbc.driver.OracleDriver', 'ACTIVE', 'system_jhyh'),
('db_sjzl', '数据治理与决策分析数据库配置', 'jdbc:postgresql://36.2.11.155:5432/60waternest?charSet=utf-8', 'watersys', 'watersys', 'org.postgresql.Driver', 'ACTIVE', 'system_sjzl'),
('db_yzt', '一张图', 'jdbc:oracle:thin:@36.2.14.123:1521:uboss', 'jxjtyzt', 'jxJTyZt!^202409', 'oracle.jdbc.OracleDriver', 'ACTIVE', 'system_yzt');

-- 迁移 API 定义
INSERT INTO sys_api_definition (api_code, api_name, api_path, http_method, operation_type, execute_mode, data_source_code, status, system_code) VALUES
            ('getYztsomething', '【公路路网-首页-固定资产投资】-统计固定资产汇总信息', '/getYztsomething', 'POST', 'QUERY', 'SINGLE', 'db_yzt', 'PUBLISHED', 'system_yzt'),
            ('getUserInfo', '测试查询', '/getUserInfo', 'POST', 'QUERY', 'SINGLE', 'db_sjzl', 'PUBLISHED', 'system_sjzl'),
            ('getBanner', '查询banner列表', '/getBanner', 'POST', 'QUERY', 'SINGLE', 'db_127_mysql', 'PUBLISHED', 'system_guanwang'),
            ('insertTestLxl', '计划养护系统-插入测试表', '/insertTestLxl', 'POST', 'INSERT', 'SINGLE', 'db_jhyh', 'PUBLISHED', 'system_jhyh'),
            ('updateZjdw', '更新资金到位', '/updateZjdw', 'POST', 'UPDATE', 'SINGLE', 'db_csxt', 'PUBLISHED', 'system_csxt'),
            ('getJhyhXzqh', '计划养护-获取行政区划接口', '/getJhyhXzqh', 'POST', 'QUERY', 'SINGLE', 'db_jhyh', 'PUBLISHED', 'system_jhyh'),
            ('deleteTestLxl', '计划养护系统删除测试', '/deleteTestLxl', 'POST', 'DELETE', 'SINGLE', 'db_jhyh', 'PUBLISHED', 'system_jhyh');


-- 迁移 API SQL 定义
INSERT INTO sys_api_sql_definition (api_code, sql_text, sql_type, enabled_flag) VALUES
('getYztsomething', 'with curent as  (SELECT\r\n	sum( ndmb ) ndmb,\r\n	sum( bnljwc ) bnljwc,\r\n	:sjnf as sjnf,\r\n	round( sum( bnljwc ) / sum( ndmb ), 4 ) * 100 wcl \r\nFROM\r\n	dws_gl_jcxx_gdzctz \r\nWHERE\r\n	1 = 1 \r\n	AND gdlx IN ( ''高速公路'', ''农村公路'', ''国省干线'' ) #[ \r\n	AND sjnf =: sjnf ] #[ \r\n	AND nvl( xzqhdm, ''36'' ) LIKE : xzqhdm ] #[@\r\nIF\r\n	( : sjlx == ''GS'' ) \r\n	AND xzdjlx = ''1'' ] #[@\r\nIF\r\n	( : sjlx == ''GSD'' ) \r\n	AND xzdjlx = ''2'' ] #[@\r\nIF\r\n	( : sjlx == ''NCGL'' ) \r\n	AND xzdjlx = ''3'']\r\n),\r\n	last as (\r\nSELECT\r\n	sum( ndmb ) ndmb,\r\n	sum( bnljwc ) bnljwc,\r\n	:sjnf-1 as sjnf,\r\n	round( sum( bnljwc ) / sum( ndmb ), 4 ) * 100 wcl \r\nFROM\r\n	dws_gl_jcxx_gdzctz \r\nWHERE\r\n	1 = 1 \r\n	AND gdlx IN ( ''高速公路'', ''农村公路'', ''国省干线'' ) #[ \r\n	AND sjnf =: sjnf-1 ] #[ \r\n	AND nvl( xzqhdm, ''36'' ) LIKE : xzqhdm ] #[@\r\nIF\r\n	( : sjlx == ''GS'' ) \r\n	AND xzdjlx = ''1'' ] #[@\r\nIF\r\n	( : sjlx == ''GSD'' ) \r\n	AND xzdjlx = ''2'' ] #[@\r\nIF\r\n	( : sjlx == ''NCGL'' ) \r\n	AND xzdjlx = ''3'']\r\n)\r\nselect aa.*,aa.bnljwc,bb.bnljwc, round((aa.bnljwc-bb.bnljwc)/bb.bnljwc,4)*100 as tbzzl  from curent aa LEFT JOIN last bb on aa.sjnf=bb.sjnf+1', 'SELECT', true),
('getUserInfo', 'select * from user_info where 1=1 #[and user_name like :user_name]  #[and user_code=:user_code]', 'SELECT', true),
('getBanner', 'select * from banner where 1=1 #[ and title like :title]', 'SELECT', true),
('insertTestLxl', 'INSERT INTO test_lxl (\r\n    id\r\n    #[@if(:name!=null)  , name ]\r\n    #[@if(:sex!=null) , sex]  \r\n)\r\nVALUES (\r\n    sys_guid()\r\n    #[@if(:name!=null)  , :name]   \r\n    #[@if(:sex!=null)  , :sex] \r\n);', 'INSERT', true),
('updateZjdw', 'UPDATE \r\n#[@if(:type==''资金到位'') XM_ZJDW]\r\n#[@if(:type==''资金拨付'') XM_ZJBF]\r\n set SBTHCD = 11, XSBZT = ''未上报'', SSBZT = ''未上报'', SFTH = ''是'', SHZT = ''未审核''\r\nWHERE \r\nXMBM  in (\r\nSELECT\r\n	xmbm \r\nFROM\r\n	XMJBXX \r\nWHERE 1=1\r\n	and xmmc = :xmmc\r\n	AND （gcfl = :jsxz or jsxz =:jsxz)\r\n)', 'UPDATE', true),
('getJhyhXzqh', 'select * from xtgl_xzqh where 1=1 #[and name like :name] #[and id=:id]', 'SELECT', true),
('deleteTestLxl', 'delete from test_lxl where id in (:id)', 'DELETE', true);

-- =============== RBAC ===================
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
parent_id BIGINT,
name VARCHAR(128) NOT NULL,
sort_no INT,
status VARCHAR(32) DEFAULT 'ACTIVE',
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(64) NOT NULL UNIQUE,
password VARCHAR(256) NOT NULL,
nickname VARCHAR(64),
dept_id BIGINT,
status VARCHAR(32) DEFAULT 'ACTIVE',
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
code VARCHAR(64) NOT NULL UNIQUE,
name VARCHAR(128) NOT NULL,
status VARCHAR(32) DEFAULT 'ACTIVE'
);

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
parent_id BIGINT,
name VARCHAR(128) NOT NULL,
path VARCHAR(256),
component VARCHAR(256),
icon VARCHAR(64),
sort_no INT,
visible_flag BOOLEAN DEFAULT TRUE,
keep_alive_flag BOOLEAN DEFAULT FALSE,
type_flag VARCHAR(32) DEFAULT 'MENU',
permission_code VARCHAR(128),
iframe_url VARCHAR(512)
);

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
user_id BIGINT NOT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (user_id, role_id)
);

DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
role_id BIGINT NOT NULL,
menu_id BIGINT NOT NULL,
PRIMARY KEY (role_id, menu_id)
);

DROP TABLE IF EXISTS sys_api_whitelist;
CREATE TABLE sys_api_whitelist (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
url_pattern VARCHAR(256) NOT NULL,
remark VARCHAR(256)
);

INSERT INTO sys_user (username, password, nickname) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员');
INSERT INTO sys_role (code, name) VALUES ('ADMIN', '超级管理员');
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_api_whitelist (url_pattern, remark) VALUES ('/admin/auth/login', '登录接口');
INSERT INTO sys_api_whitelist (url_pattern, remark) VALUES ('/admin/report/execute/preview', '报表预览');
INSERT INTO sys_api_whitelist (url_pattern, remark) VALUES ('/admin/report/execute/instance/*', '报表实例');

INSERT INTO sys_menu (id, parent_id, name, path, icon, sort_no) VALUES
(1, NULL, '系统管理', '', 'Setting', 10),
(2, 1, '数据源管理', '/data-source-manage', 'Coin', 1),
(3, 1, '子系统管理', '/system-manage', 'Tools', 2),
(4, 1, '代码生成', '/code-gen', 'MagicStick', 3),
(5, 1, '调用日志', '/call-log', 'Document', 4),
(6, 1, '用户管理', '/sys-user', 'User', 5),
(7, 1, '角色管理', '/sys-role', 'Avatar', 6),
(8, 1, '菜单管理', '/sys-menu', 'Menu', 7),
(9, 1, '部门管理', '/sys-dept', 'Location', 4),
(10, NULL, 'API开放平台', '', 'Monitor', 20),
(11, 10, 'SQL工作台', '/sql-workbench', 'DataBoard', 1),
(12, 10, '接口管理', '/api-manage', 'Monitor', 2),
(20, NULL, '报表生成器', '', 'PieChart', 30),
(21, 20, '报表模板管理', '/report-template', 'Tickets', 1),
(22, 20, '报表实例管理', '/report-instance', 'DataAnalysis', 2);

INSERT INTO sys_menu (id, parent_id, name, path, icon, sort_no, type_flag, permission_code) VALUES
(101, 6, '新增用户', '', '', 1, 'BUTTON', 'sys:user:add'),
(102, 6, '编辑用户', '', '', 2, 'BUTTON', 'sys:user:update'),
(103, 6, '删除用户', '', '', 3, 'BUTTON', 'sys:user:delete'),
(104, 6, '分配角色', '', '', 4, 'BUTTON', 'sys:user:assign_role'),
(105, 6, '导出Excel', '', '', 5, 'BUTTON', 'sys:user:export'),
(111, 7, '新增角色', '', '', 1, 'BUTTON', 'sys:role:add'),
(112, 7, '编辑角色', '', '', 2, 'BUTTON', 'sys:role:update'),
(113, 7, '删除角色', '', '', 3, 'BUTTON', 'sys:role:delete'),
(114, 7, '分配菜单', '', '', 4, 'BUTTON', 'sys:role:assign_menu'),
(115, 7, '导出Excel', '', '', 5, 'BUTTON', 'sys:role:export'),
(121, 8, '新增菜单', '', '', 1, 'BUTTON', 'sys:menu:add'),
(122, 8, '编辑菜单', '', '', 2, 'BUTTON', 'sys:menu:update'),
(123, 8, '删除菜单', '', '', 3, 'BUTTON', 'sys:menu:delete'),
(131, 3, '新增系统', '', '', 1, 'BUTTON', 'sys:dict:add'),
(132, 3, '编辑系统', '', '', 2, 'BUTTON', 'sys:dict:update'),
(133, 3, '删除系统', '', '', 3, 'BUTTON', 'sys:dict:delete'),
(134, 3, '导出Excel', '', '', 4, 'BUTTON', 'sys:dict:export');

CREATE TABLE sys_wf_def (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(100) NOT NULL,
type_code VARCHAR(50) DEFAULT 'GENERAL',
xml_data TEXT NOT NULL,
status VARCHAR(20) DEFAULT 'ACTIVE',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lc_form_definition (
id VARCHAR(64) PRIMARY KEY,
name VARCHAR(128) NOT NULL,
schema_json LONGTEXT
);

CREATE TABLE lc_business_instance (
id VARCHAR(64) PRIMARY KEY,
form_id VARCHAR(64) NOT NULL,
proc_ins_id VARCHAR(64),
data_content LONGTEXT,
status VARCHAR(20) DEFAULT 'DRAFT',
creator VARCHAR(64),
create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lc_audit_log (
id VARCHAR(64) PRIMARY KEY,
business_id VARCHAR(64) NOT NULL,
task_id VARCHAR(64),
task_name VARCHAR(128),
assignee VARCHAR(64),
action VARCHAR(20),
comment TEXT,
create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO lc_form_definition (id, name, schema_json) VALUES
('form_1001', '请假申请表', '[{"label":"请假天数","field":"days","type":"number"},{"label":"请假原因","field":"reason","type":"text"}]');

INSERT INTO sys_menu (id, parent_id, name, path, icon, sort_no) VALUES
(30, NULL, '流程管理', '', 'Connection', 40),
(31, 30, '流程定义管理', '/workflow/def', 'EditPen', 1),
(32, 30, '发起流程', '/workflow/start', 'Position', 2),
(33, 30, '我的待办', '/workflow/tasks', 'List', 3);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 30), (1, 31), (1, 32), (1, 33),
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 10), (1, 11), (1, 12),
(1, 20), (1, 21), (1, 22),
(1, 101), (1, 102), (1, 103), (1, 104), (1, 105),
(1, 111), (1, 112), (1, 113), (1, 114), (1, 115),
(1, 121), (1, 122), (1, 123),
(1, 131), (1, 132), (1, 133), (1, 134);

