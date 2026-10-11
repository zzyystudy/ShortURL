-- 在已创建四张业务表的 shortURL 数据库中执行；应用不会自动运行本文件。
-- Base62 区分大小写，唯一索引也必须区分大小写。
ALTER TABLE url_map
    MODIFY compression_code VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin
    NOT NULL COMMENT '短码';

-- 保留已有域名配置，添加当前本地应用的访问域名。
INSERT INTO domain_conf (domain_value, protocol, domain_status)
SELECT 'localhost:8778', 'http', 1
WHERE NOT EXISTS (
    SELECT 1 FROM domain_conf WHERE domain_value = 'localhost:8778'
);
