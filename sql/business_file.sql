-- ----------------------------
-- Table structure for image_file
-- ----------------------------
DROP TABLE IF EXISTS `image_file`;
CREATE TABLE `image_file` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_name` varchar(512) DEFAULT NULL COMMENT '原始文件名',
    `file_url` varchar(1024) DEFAULT NULL COMMENT '完整访问URL',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
    `storage_type` varchar(32) DEFAULT NULL COMMENT '存储类型: minio/aliyun/tencent',
    `upload_user_id` bigint DEFAULT NULL COMMENT '上传用户ID',
    `ref_count` int NOT NULL DEFAULT 0 COMMENT '引用计数，0-未绑定 >0-已绑定',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-临时 1-已绑定 2-已废弃',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图片业务文件表';

-- ----------------------------
-- Table structure for audio_file
-- ----------------------------
DROP TABLE IF EXISTS `track_file`;
CREATE TABLE `track_file` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_name` varchar(512) DEFAULT NULL COMMENT '原始文件名',
    `media_file_id` varchar(64) DEFAULT NULL COMMENT 'VOD媒体文件ID',
    `media_url` varchar(1024) DEFAULT NULL COMMENT 'VOD播放地址',
    `file_url` varchar(1024) DEFAULT NULL COMMENT 'MinIO文件URL',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
    `media_duration` decimal(10, 2) DEFAULT NULL COMMENT '音频时长(秒)',
    `upload_user_id` bigint DEFAULT NULL COMMENT '上传用户ID',
    `ref_count` int NOT NULL DEFAULT '0' COMMENT '引用计数，0-未绑定 >0-已绑定',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-临时 1-已绑定 2-已废弃',
    `file_hash` varchar(64) DEFAULT NULL COMMENT '文件MD5哈希',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT
  '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_upload_user_id` (`upload_user_id`),
    KEY `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='声音业务文件表';
