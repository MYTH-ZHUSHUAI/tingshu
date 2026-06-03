/*
 Navicat Premium Dump SQL

 Source Server         : mysql@192.168.1.140
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : 192.168.1.140:3306
 Source Schema         : tingshu_album

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 27/05/2026 15:05:23
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for album_attribute_value
-- ----------------------------
DROP TABLE IF EXISTS `album_attribute_value`;
CREATE TABLE `album_attribute_value` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `album_id` bigint NOT NULL DEFAULT '0' COMMENT '专辑id',
    `attribute_id` bigint NOT NULL DEFAULT '0' COMMENT '属性id',
    `value_id` bigint NOT NULL DEFAULT '0' COMMENT '属性值id',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_album_id` (`album_id`),
    KEY `idx_value_id` (`value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专辑属性值关联表';

-- ----------------------------
-- Table structure for album_info
-- ----------------------------
DROP TABLE IF EXISTS `album_info`;
CREATE TABLE `album_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id',
    `album_title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
    `category3_id` bigint NOT NULL DEFAULT '0' COMMENT '三级分类id',
    `album_intro` varchar(200) NOT NULL DEFAULT '0' COMMENT '专辑简介',
    `cover_url` varchar(200) NOT NULL DEFAULT '' COMMENT '专辑封面原图，尺寸不固定，最大尺寸为960*960（像素）',
    `include_track_count` int unsigned DEFAULT '0' COMMENT '专辑包含声音总数',
    `is_finished` char(1) NOT NULL DEFAULT '0' COMMENT '专辑是否完结：0-否；1-完结；',
    `estimated_track_count` int unsigned NOT NULL DEFAULT '0' COMMENT '预计更新多少集',
    `album_rich_intro` text COMMENT '专辑简介，富文本',
    `quality_score` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '专辑评分',
    `pay_type` char(4) NOT NULL DEFAULT '0101' COMMENT '付费类型: 0101-免费、0102-vip免费、0103-付费',
    `price_type` char(4) DEFAULT NULL COMMENT '价格类型： 0201-单集 0202-整专辑 【声音购买不支持折扣】',
    `price` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '原价',
    `discount` decimal(2, 1) NOT NULL DEFAULT '-1.0' COMMENT '0.1-9.9  不打折 -1',
    `vip_discount` decimal(2, 1) NOT NULL DEFAULT '-1.0' COMMENT '0.1-9.9 不打折 -1',
    `tracks_for_free` int NOT NULL DEFAULT '0' COMMENT '免费试听集数',
    `seconds_for_free` int NOT NULL DEFAULT '0' COMMENT '每集免费试听秒数',
    `buy_notes` text COMMENT '购买须知，富文本',
    `selling_point` text COMMENT '专辑卖点，富文本',
    `is_open` char(1) NOT NULL DEFAULT '1' COMMENT '是否开放',
    `status` char(4) DEFAULT NULL COMMENT '专辑状态 0301-审核通过 0302-审核不通过',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_category3_id` (`category3_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1594 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专辑信息';

-- ----------------------------
-- Table structure for album_stat
-- ----------------------------
DROP TABLE IF EXISTS `album_stat`;
CREATE TABLE `album_stat` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `album_id` bigint DEFAULT '0' COMMENT '专辑id',
    `stat_type` varchar(10) NOT NULL DEFAULT '0' COMMENT '统计类型：0401-播放量 0402-订阅量 0403-购买量 0403-评论数',
    `stat_num` int unsigned NOT NULL DEFAULT '0' COMMENT '统计数目',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_album_id` (`album_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6373 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专辑统计';

-- ----------------------------
-- Table structure for base_attribute
-- ----------------------------
DROP TABLE IF EXISTS `base_attribute`;
CREATE TABLE `base_attribute` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `category1_id` bigint NOT NULL DEFAULT '0' COMMENT '1级分类id',
    `attribute_name` varchar(200) NOT NULL DEFAULT '' COMMENT '属性显示名称',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_category3_id` (`category1_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属性表';

-- ----------------------------
-- Table structure for base_attribute_value
-- ----------------------------
DROP TABLE IF EXISTS `base_attribute_value`;
CREATE TABLE `base_attribute_value` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `attribute_id` bigint NOT NULL DEFAULT '0' COMMENT '属性id',
    `value_name` varchar(100) NOT NULL DEFAULT '' COMMENT '属性值名称',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_attribute_id` (`attribute_id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属性值表';

-- ----------------------------
-- Table structure for base_category1
-- ----------------------------
DROP TABLE IF EXISTS `base_category1`;
CREATE TABLE `base_category1` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(10) NOT NULL COMMENT '分类名称',
    `order_num` int NOT NULL DEFAULT '0' COMMENT '排序',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一级分类表';

-- ----------------------------
-- Table structure for base_category2
-- ----------------------------
DROP TABLE IF EXISTS `base_category2`;
CREATE TABLE `base_category2` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(100) NOT NULL DEFAULT '' COMMENT '二级分类名称',
    `category1_id` bigint NOT NULL DEFAULT '0' COMMENT '一级分类编号',
    `order_num` int NOT NULL DEFAULT '0',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category1_id` (`category1_id`)
) ENGINE=InnoDB AUTO_INCREMENT=193 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二级分类表';

-- ----------------------------
-- Table structure for base_category3
-- ----------------------------
DROP TABLE IF EXISTS `base_category3`;
CREATE TABLE `base_category3` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(100) NOT NULL DEFAULT '' COMMENT '三级分类名称',
    `category2_id` bigint NOT NULL DEFAULT '0' COMMENT '二级分类编号',
    `order_num` int NOT NULL DEFAULT '0' COMMENT '排序',
    `is_top` tinyint NOT NULL DEFAULT '0',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category2_id` (`category2_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='三级分类表';

-- ----------------------------
-- Table structure for track_info
-- ----------------------------
DROP TABLE IF EXISTS `track_info`;
CREATE TABLE `track_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` varchar(255) DEFAULT NULL COMMENT '用户id',
    `album_id` bigint NOT NULL DEFAULT '0' COMMENT '专辑id',
    `track_title` varchar(200) NOT NULL DEFAULT '' COMMENT '声音标题',
    `order_num` int NOT NULL DEFAULT '0' COMMENT '声音在专辑中的排序值，从1开始依次递增，值越小排序越前',
    `track_intro` varchar(255) DEFAULT NULL COMMENT '声音简介',
    `track_rich_intro` text COMMENT '声音简介，富文本',
    `cover_url` varchar(255) DEFAULT NULL COMMENT '声音封面图url',
    `media_duration` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '声音媒体时长，单位秒',
    `media_file_id` varchar(30) DEFAULT NULL COMMENT '媒体文件的唯一标识',
    `media_url` varchar(200) DEFAULT '' COMMENT '媒体播放地址',
    `media_size` bigint NOT NULL DEFAULT '0' COMMENT '音频文件大小，单位字节',
    `media_type` varchar(10) NOT NULL DEFAULT '' COMMENT '声音媒体类型',
    `source` char(4) NOT NULL DEFAULT '1' COMMENT '声音来源：0601-用户原创 0602-上传',
    `is_open` char(1) NOT NULL DEFAULT '1' COMMENT '是否开放',
    `status` char(4) NOT NULL DEFAULT '0' COMMENT '声音状态 0501-审核通过 0502"-审核不通过',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_album_id` (`album_id`)
) ENGINE=InnoDB AUTO_INCREMENT=51942 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='声音信息';

-- ----------------------------
-- Table structure for track_stat
-- ----------------------------
DROP TABLE IF EXISTS `track_stat`;
CREATE TABLE `track_stat` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `track_id` bigint NOT NULL DEFAULT '0' COMMENT '声音id',
    `stat_type` varchar(10) NOT NULL DEFAULT '0' COMMENT '统计类型：0701-播放量 0702-收藏量 0703-点赞量 0704-评论数',
    `stat_num` int NOT NULL DEFAULT '0' COMMENT '统计数目',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_track_id` (`track_id`)
) ENGINE=InnoDB AUTO_INCREMENT=207762 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='声音统计';

-- ----------------------------
-- View structure for base_category_view
-- ----------------------------
DROP VIEW IF EXISTS `base_category_view`;
CREATE
ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `base_category_view` AS
select `c3`.`id` AS `id`, `c1`.`id` AS `category1_id`, `c1`.`name` AS `category1_name`, `c2`.`id` AS `category2_id`, `c2`.`name` AS `category2_name`, `c3`.`id` AS `category3_id`,
    `c3`.`name` AS `category3_name`, `c3`.`create_time` AS `create_time`, `c3`.`update_time` AS `update_time`, `c3`.`is_deleted` AS `is_deleted`
from ((`base_category1` `c1` join `base_category2` `c2` on ((`c2`.`category1_id` = `c1`.`id`))) join `base_category3` `c3` on ((`c3`.`category2_id` = `c2`.`id`)));

SET
FOREIGN_KEY_CHECKS = 1;
