-- 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '成员ID',
    `acct` VARCHAR(255) NOT NULL COMMENT '登录帐号',
    `flag` INT DEFAULT 0 COMMENT '成员标志',
    `name` VARCHAR(255) DEFAULT NULL COMMENT '成员名称',
    `pwd` BINARY(16) DEFAULT NULL COMMENT '成员密码',
    `twofa_scheme` VARCHAR(255) DEFAULT NULL COMMENT '双因子认证方案',
    `twofa_secret` VARCHAR(255) DEFAULT NULL COMMENT '双因子认证密钥',
    `role_ids` TEXT DEFAULT NULL COMMENT '-- 角色ID列表，如["1300880097435324418", "1300880097435324419"]',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户表';
INSERT INTO `user` (
    `id`, `acct`, `flag`, `name`, `pwd`, `twofa_scheme`, `twofa_secret`, `role_ids`, `create_time`
) VALUES (
    1300880097435324416,
    'admin',
    0,
    '管理员',
    UNHEX('21232F297A57A5A743894A0E4A801FC3'),
    NULL,
    '',
    '["1376966563122515968"]',
    '2024-10-01 10:10:10'
);

-- 角色表
CREATE TABLE `role` (
    `id` BIGINT NOT NULL COMMENT '角色ID',
    `name` VARCHAR(255) NOT NULL COMMENT '角色名称',
    `is_admin` TINYINT DEFAULT 0 COMMENT '是否是管理员角色，0否，1是',
    `status` TINYINT DEFAULT 0 COMMENT '角色状态，0正常，1禁用',
    `permissions` TEXT DEFAULT NULL COMMENT '权限列表，如["system:user:add", "system:role:remove"]',
    `remark` TEXT DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '角色表';

INSERT INTO `role` (
    `id`, `name`, `is_admin`, `status`, `permissions`, `remark`, `create_time`, `update_time`
) VALUES (
    1376966563122515968,
    '管理员',
    1,
    0,
    NULL,
    NULL,
    '2025-05-01 09:00:00',
    '2025-05-01 09:00:00'
);

-- 用户会话登录日志表
CREATE TABLE `user_session_log` (
    `id` BIGINT NOT NULL COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `ip` VARCHAR(255) NOT NULL COMMENT '登录IP',
    `location` VARCHAR(255) DEFAULT NULL COMMENT '登录地址',
    `user_agent` VARCHAR(255) DEFAULT NULL COMMENT '用户代理',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户登录日志表';

-- 用户操作日志表
CREATE TABLE `user_operation_log` (
    `id` BIGINT NOT NULL COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `ip` VARCHAR(255) NOT NULL COMMENT '操作IP',
    `location` VARCHAR(255) DEFAULT NULL COMMENT '操作地址',
    `user_agent` VARCHAR(255) DEFAULT NULL COMMENT '用户代理',
    `module` VARCHAR(128) NOT NULL COMMENT '操作模块',
    `module_id` VARCHAR(128) NOT NULL COMMENT '操作模块ID',
    `operation_type` TINYINT NOT NULL COMMENT '操作类型（1:新增, 2:修改, 3:删除）',
    `uri` VARCHAR(255) NOT NULL COMMENT '操作URI',
    `content` TEXT NOT NULL COMMENT '操作内容',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户操作日志表';
