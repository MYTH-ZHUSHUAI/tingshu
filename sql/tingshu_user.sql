/*
 Navicat Premium Dump SQL

 Source Server         : mysql@192.168.1.140
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : 192.168.1.140:3306
 Source Schema         : tingshu_user

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 03/06/2026 16:01:49
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_certification
-- ----------------------------
DROP TABLE IF EXISTS `user_certification`;
CREATE TABLE `user_certification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id',
    `id_card1_url` varchar(255) DEFAULT NULL COMMENT '身份证地址1',
    `id_card2_url` varchar(255) DEFAULT NULL COMMENT '身份证地址2',
    `face_url` varchar(255) DEFAULT NULL COMMENT '人脸图片地址',
    `result_data` text COMMENT '比对结果数据',
    `operate_user_id` bigint DEFAULT NULL COMMENT '日志操作用户',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户认证';

-- ----------------------------
-- Table structure for user_collect
-- ----------------------------
DROP TABLE IF EXISTS `user_collect`;
CREATE TABLE `user_collect` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `track_id` bigint NOT NULL DEFAULT '0' COMMENT '声音ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `phone` varchar(11) DEFAULT NULL COMMENT '手机',
    `password` varchar(50) DEFAULT NULL COMMENT '密码',
    `wx_open_id` varchar(50) NOT NULL DEFAULT '' COMMENT '微信openId',
    `nickname` varchar(100) DEFAULT '' COMMENT 'nickname',
    `avatar_url` varchar(500) DEFAULT '' COMMENT '主播用户头像图片',
    `is_vip` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '用户是否为VIP会员',
    `vip_expire_time` datetime DEFAULT NULL COMMENT '当前VIP到期时间，即失效时间',
    `gender` tinyint DEFAULT NULL COMMENT '性别',
    `birthday` date DEFAULT NULL COMMENT '出生年月',
    `intro` varchar(255) DEFAULT NULL COMMENT '简介',
    `certification_type` tinyint DEFAULT NULL COMMENT '主播认证类型',
    `certification_status` tinyint DEFAULT NULL COMMENT '认证状态',
    `status` char(4) NOT NULL DEFAULT '0' COMMENT '状态',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_wx_open_id` (`wx_open_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';

-- ----------------------------
-- Table structure for user_listen_process
-- ----------------------------
DROP TABLE IF EXISTS `user_listen_process`;
CREATE TABLE `user_listen_process` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id',
    `album_id` bigint NOT NULL DEFAULT '0' COMMENT '专辑id',
    `track_id` bigint NOT NULL DEFAULT '0' COMMENT '声音id，声音id为0时，浏览的是专辑',
    `break_second` decimal(10, 2) DEFAULT NULL COMMENT '相对于音频开始位置的播放跳出位置，单位为秒。比如当前音频总时长60s，本次播放到音频第25s处就退出或者切到下一首，那么break_second就是25',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户播放进度表';

-- ----------------------------
-- Table structure for user_paid_album
-- ----------------------------
DROP TABLE IF EXISTS `user_paid_album`;
CREATE TABLE `user_paid_album` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no` varchar(50) NOT NULL DEFAULT '0' COMMENT '订单号',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户ID',
    `album_id` bigint NOT NULL DEFAULT '0' COMMENT '专辑id',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户已付款专辑';

-- ----------------------------
-- Table structure for user_paid_track
-- ----------------------------
DROP TABLE IF EXISTS `user_paid_track`;
CREATE TABLE `user_paid_track` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no` varchar(50) NOT NULL DEFAULT '0' COMMENT '订单号',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户ID',
    `album_id` bigint DEFAULT NULL COMMENT '专辑id',
    `track_id` bigint NOT NULL DEFAULT '0' COMMENT '声音id',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户已付款声音';

-- ----------------------------
-- Table structure for user_stat
-- ----------------------------
DROP TABLE IF EXISTS `user_stat`;
CREATE TABLE `user_stat` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id',
    `stat_type` int NOT NULL DEFAULT '0' COMMENT '统计类型',
    `stat_num` int NOT NULL DEFAULT '0' COMMENT '统计数目',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_track_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户统计';

-- ----------------------------
-- Table structure for user_subscribe
-- ----------------------------
DROP TABLE IF EXISTS `user_subscribe`;
CREATE TABLE `user_subscribe` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `album_id` bigint NOT NULL DEFAULT '0' COMMENT '专辑ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记（0:不可用 1:可用）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_album_id` (`album_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户订阅表';

-- ----------------------------
-- Table structure for user_vip_service
-- ----------------------------
DROP TABLE IF EXISTS `user_vip_service`;
CREATE TABLE `user_vip_service` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `order_no` varchar(50) NOT NULL DEFAULT '' COMMENT '订单号',
    `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id',
    `start_time` datetime DEFAULT NULL COMMENT '开始生效日期',
    `expire_time` datetime DEFAULT NULL COMMENT '到期时间',
    `is_auto_renew` tinyint NOT NULL DEFAULT '0' COMMENT '是否自动续费',
    `next_renew_time` datetime DEFAULT NULL COMMENT '下次自动续费时间',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uniq_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户vip服务记录表';

-- ----------------------------
-- Table structure for vip_service_config
-- ----------------------------
DROP TABLE IF EXISTS `vip_service_config`;
CREATE TABLE `vip_service_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(20) NOT NULL DEFAULT '' COMMENT '服务名称',
    `price` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '原价，单位元，用于营销展示',
    `discount_price` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '折后价，单位元，即实际价格',
    `intro` varchar(50) DEFAULT NULL COMMENT '优惠简介',
    `rich_intro` varchar(300) DEFAULT NULL COMMENT '服务简介，富文本',
    `service_month` int DEFAULT NULL COMMENT '服务月数',
    `image_url` varchar(100) NOT NULL DEFAULT '0' COMMENT '服务图片url',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='vip服务配置表';

SET
FOREIGN_KEY_CHECKS = 1;
