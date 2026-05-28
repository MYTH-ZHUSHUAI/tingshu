CREATE TABLE `file_metadata` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_name` VARCHAR(512) DEFAULT NULL COMMENT '原始文件名',
    `file_key` VARCHAR(256) DEFAULT NULL COMMENT '存储路径/对象名',
    `file_url` VARCHAR(1024) DEFAULT NULL COMMENT '完整访问URL',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `file_hash` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5哈希值',
    `storage_type` VARCHAR(32) DEFAULT NULL COMMENT '存储类型: minio/vod',
    `bucket_name` VARCHAR(128) DEFAULT NULL COMMENT '存储桶名称',
    `upload_user_id` BIGINT DEFAULT NULL COMMENT '上传用户ID',
    `ref_count` INT DEFAULT 0 COMMENT '引用计数',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-临时 1-已绑定 2-已废弃',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_hash` (`file_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元数据表';

