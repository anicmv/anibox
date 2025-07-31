DROP TABLE IF EXISTS `image`;
CREATE TABLE `image`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `user_id`     bigint       NOT NULL COMMENT '用户',
    `short_key`   varchar(255) NOT NULL COMMENT '图片短链key',
    `path`        varchar(255) NOT NULL COMMENT '保存路径',
    `name`        varchar(255) NOT NULL COMMENT '保存名称',
    `origin_name` varchar(255) NOT NULL COMMENT '原始名称',
    `suffix`      varchar(32)  NOT NULL COMMENT '文件后缀',
    `alias_name`  varchar(255)  DEFAULT NULL COMMENT '别名',
    `size`        decimal(8, 2) DEFAULT NULL COMMENT '图片大小(kb)',
    `md5`         varchar(32)  NOT NULL COMMENT '文件MD5',
    `sha1`        varchar(255) NOT NULL COMMENT '文件SHA1',
    `width`       int           DEFAULT NULL COMMENT '宽',
    `height`      int           DEFAULT NULL COMMENT '高',
    `permission`  tinyint       DEFAULT '0' COMMENT '访问权限',
    `uploaded_ip` varchar(255)  DEFAULT NULL COMMENT '上传IP',
    `create_time` TIMESTAMP     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `short_key_unique` (`short_key`),
    UNIQUE KEY `name_unique` (`name`),
    KEY           `alias_name_index` (`alias_name`),
    KEY           `md5_sha1_key` (`md5`,`sha1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `username`    varchar(255) NOT NULL COMMENT '用户名',
    `password`    varchar(255) NOT NULL COMMENT '密码',
    `roles`       varchar(255)          DEFAULT 'user' COMMENT '角色',
    `salt_key`    varchar(255)          DEFAULT NULL COMMENT 'jwt密钥',
    `token`       varchar(255)          DEFAULT NULL COMMENT 'jwt',
    `image_num`   bigint                DEFAULT '0' COMMENT '图片数量',
    `root_path`   varchar(255)          DEFAULT NULL COMMENT '图片根目录',
    `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '状态',
    `create_time` TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_token` (`token`),
    UNIQUE KEY `index_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `name`        varchar(100) NOT NULL COMMENT '标签名称',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `image_tag`;
CREATE TABLE `image_tag`
(
    `image_id` bigint NOT NULL COMMENT '图片ID',
    `tag_id`   bigint NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`image_id`, `tag_id`),
    KEY        `tag_id_index` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `album`;
CREATE TABLE `album`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `name`        varchar(255) NOT NULL COMMENT '相册名称',
    `description` varchar(500) DEFAULT NULL COMMENT '相册描述',
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `album_name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `image_album`;
CREATE TABLE `image_album`
(
    `image_id` bigint NOT NULL COMMENT '图片ID',
    `album_id` bigint NOT NULL COMMENT '相册ID',
    PRIMARY KEY (`image_id`, `album_id`),
    KEY        `album_id_index` (`album_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
