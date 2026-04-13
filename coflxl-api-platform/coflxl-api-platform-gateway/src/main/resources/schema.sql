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
