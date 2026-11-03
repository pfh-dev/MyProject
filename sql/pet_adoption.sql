-- =============================================================
-- 宠物领养救助平台 数据库初始化脚本
-- 数据库: pet_adoption
-- 版本: 1.0.0
-- 说明: 14 张业务表 + 初始化数据（分类/轮播/资讯/示例宠物）
--       管理员账号由后端 DataInitializer 在首次启动时自动创建
--       (账号 admin / 密码 admin123，BCrypt 加密)
-- =============================================================

CREATE DATABASE IF NOT EXISTS `pet_adoption` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `pet_adoption`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------
-- 1. 用户表（统一账号 + 角色）
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `phone`       VARCHAR(200) DEFAULT NULL COMMENT '手机号(AES加密)',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender`      VARCHAR(10)  DEFAULT NULL COMMENT '性别:男/女',
    `age`         INT          DEFAULT NULL COMMENT '年龄',
    `address`     VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色:ADMIN/STAFF/VOLUNTEER/USER',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态:0正常 1禁用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------
-- 2. 宠物分类表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `pet_category`;
CREATE TABLE `pet_category` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `icon`       VARCHAR(255) DEFAULT NULL COMMENT '图标',
    `sort`       INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '宠物分类表';

-- ---------------------------------------------------------------
-- 3. 宠物档案表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          VARCHAR(50)   NOT NULL COMMENT '宠物名',
    `category_id`   BIGINT        DEFAULT NULL COMMENT '分类ID',
    `breed`         VARCHAR(50)   DEFAULT NULL COMMENT '品种',
    `gender`        VARCHAR(10)   DEFAULT NULL COMMENT '性别:公/母',
    `age`           VARCHAR(20)   DEFAULT NULL COMMENT '年龄描述',
    `vaccine`       VARCHAR(50)   DEFAULT NULL COMMENT '疫苗情况',
    `sterilization` VARCHAR(20)   DEFAULT NULL COMMENT '是否绝育:是/否',
    `description`   VARCHAR(500)  DEFAULT NULL COMMENT '简介',
    `detail`        TEXT          COMMENT '详情(富文本)',
    `cover_img`     VARCHAR(255)  DEFAULT NULL COMMENT '封面图',
    `images`        TEXT          COMMENT '图集(逗号分隔)',
    `status`        VARCHAR(20)   NOT NULL DEFAULT '待领养' COMMENT '领养状态:待领养/领养中/已领养',
    `source`        VARCHAR(20)   NOT NULL DEFAULT 'ADMIN' COMMENT '来源:ADMIN/USER',
    `review_status` VARCHAR(20)   NOT NULL DEFAULT '已通过' COMMENT '审核状态:待审核/已通过/已驳回',
    `create_user_id` BIGINT       DEFAULT NULL COMMENT '创建人ID',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_pet_status` (`status`),
    KEY `idx_pet_category` (`category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '宠物档案表';

-- ---------------------------------------------------------------
-- 4. 领养申请表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `adopt_application`;
CREATE TABLE `adopt_application` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pet_id`          BIGINT        NOT NULL COMMENT '宠物ID',
    `user_id`         BIGINT        NOT NULL COMMENT '申请人ID',
    `applicant_name`  VARCHAR(50)   DEFAULT NULL COMMENT '申请人姓名',
    `phone`           VARCHAR(200)  DEFAULT NULL COMMENT '联系电话(AES加密)',
    `address`         VARCHAR(255)  DEFAULT NULL COMMENT '居住地址',
    `occupation`      VARCHAR(50)   DEFAULT NULL COMMENT '职业',
    `has_experience`  VARCHAR(20)   DEFAULT NULL COMMENT '是否有养宠经验:是/否',
    `reason`          VARCHAR(1000) DEFAULT NULL COMMENT '申请理由',
    `status`          VARCHAR(20)   NOT NULL DEFAULT '待审核' COMMENT '状态:待审核/已通过/已拒绝',
    `apply_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `audit_user_id`   BIGINT        DEFAULT NULL COMMENT '审核人ID',
    `audit_time`      DATETIME      DEFAULT NULL COMMENT '审核时间',
    `audit_remark`    VARCHAR(500)  DEFAULT NULL COMMENT '审核备注',
    PRIMARY KEY (`id`),
    KEY `idx_adopt_pet` (`pet_id`),
    KEY `idx_adopt_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '领养申请表';

-- ---------------------------------------------------------------
-- 5. 回访记录表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `follow_up`;
CREATE TABLE `follow_up` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `adopt_id`      BIGINT       NOT NULL COMMENT '领养申请ID',
    `volunteer_id`  BIGINT       DEFAULT NULL COMMENT '回访志愿者ID',
    `content`       VARCHAR(1000) DEFAULT NULL COMMENT '回访内容',
    `pet_condition` VARCHAR(200) DEFAULT NULL COMMENT '宠物状况',
    `is_normal`     VARCHAR(20)  DEFAULT NULL COMMENT '是否正常:是/否',
    `follow_time`   DATETIME     DEFAULT NULL COMMENT '回访时间',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_follow_adopt` (`adopt_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '回访记录表';

-- ---------------------------------------------------------------
-- 6. 救助线索表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `rescue`;
CREATE TABLE `rescue` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        VARCHAR(100)  NOT NULL COMMENT '标题',
    `description`  VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    `images`       TEXT          COMMENT '图片(逗号分隔)',
    `location`     VARCHAR(255)  DEFAULT NULL COMMENT '发现地点',
    `contact`      VARCHAR(200)  DEFAULT NULL COMMENT '联系方式(AES加密)',
    `status`       VARCHAR(20)   NOT NULL DEFAULT '待处理' COMMENT '状态:待处理/处理中/已救助/已驳回',
    `reporter_id`  BIGINT        DEFAULT NULL COMMENT '上报人ID',
    `assignee_id`  BIGINT        DEFAULT NULL COMMENT '处理人(志愿者)ID',
    `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    `handle_time`  DATETIME      DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    KEY `idx_rescue_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '救助线索表';

-- ---------------------------------------------------------------
-- 7. 志愿者表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `volunteer`;
CREATE TABLE `volunteer` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT       NOT NULL COMMENT '关联用户ID',
    `real_name`      VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `phone`          VARCHAR(200) DEFAULT NULL COMMENT '联系电话(AES加密)',
    `skills`         VARCHAR(255) DEFAULT NULL COMMENT '技能特长',
    `available_time` VARCHAR(255) DEFAULT NULL COMMENT '可服务时间',
    `status`         VARCHAR(20)  NOT NULL DEFAULT '申请中' COMMENT '状态:申请中/已通过/已拒绝/已离职',
    `apply_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `audit_time`     DATETIME     DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_volunteer_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '志愿者表';

-- ---------------------------------------------------------------
-- 8. 捐赠表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `donation`;
CREATE TABLE `donation` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        DEFAULT NULL COMMENT '捐赠人ID',
    `type`        VARCHAR(20)   NOT NULL COMMENT '类型:资金/物资',
    `amount`      DECIMAL(10,2) DEFAULT NULL COMMENT '金额(资金)',
    `item_name`   VARCHAR(100)  DEFAULT NULL COMMENT '物资名称(物资)',
    `quantity`    INT           DEFAULT NULL COMMENT '数量(物资)',
    `description` VARCHAR(500)  DEFAULT NULL COMMENT '说明',
    `status`      VARCHAR(20)   NOT NULL DEFAULT '待确认' COMMENT '状态:待确认/已到账/已确认',
    `donate_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
    PRIMARY KEY (`id`),
    KEY `idx_donation_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '捐赠表';

-- ---------------------------------------------------------------
-- 9. 消息通知表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '接收用户ID',
    `title`       VARCHAR(100)  DEFAULT NULL COMMENT '标题',
    `content`     VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    `type`        VARCHAR(20)   DEFAULT '系统' COMMENT '类型:系统/领养/救助/审核',
    `biz_id`      BIGINT        DEFAULT NULL COMMENT '关联业务ID',
    `is_read`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读:0否 1是',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_notify_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息通知表';

-- ---------------------------------------------------------------
-- 10. 轮播图表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`   VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `img`     VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
    `link`    VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sort`    INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`  TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0下架 1上架',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '轮播图表';

-- ---------------------------------------------------------------
-- 11. 公告/资讯表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `cover`       VARCHAR(255) DEFAULT NULL COMMENT '封面',
    `content`     TEXT         COMMENT '内容',
    `type`        VARCHAR(20)  DEFAULT '资讯' COMMENT '类型:公告/科普/资讯',
    `author`      VARCHAR(50)  DEFAULT NULL COMMENT '作者',
    `view_count`  INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告资讯表';

-- ---------------------------------------------------------------
-- 12. AI 会话表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session` (
    `id`          VARCHAR(64)  NOT NULL COMMENT '会话ID(memoryId/UUID)',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '所属用户ID',
    `title`       VARCHAR(100) DEFAULT '新会话' COMMENT '会话标题',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI会话表';

-- ---------------------------------------------------------------
-- 13. AI 消息表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`  VARCHAR(64) NOT NULL COMMENT '会话ID',
    `role`        VARCHAR(20) NOT NULL COMMENT '角色:user/assistant',
    `content`     TEXT        COMMENT '消息内容',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ai_msg_session` (`session_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI消息表';

-- ---------------------------------------------------------------
-- 14. 操作日志表
-- ---------------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `username`    VARCHAR(50)  DEFAULT NULL COMMENT '操作人账号',
    `module`      VARCHAR(50)  DEFAULT NULL COMMENT '模块',
    `operation`   VARCHAR(100) DEFAULT NULL COMMENT '操作',
    `params`      TEXT         COMMENT '参数',
    `ip`          VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';

-- =============================================================
-- 初始化数据
-- =============================================================

-- 宠物分类
INSERT INTO `pet_category` (`id`, `name`, `icon`, `sort`) VALUES
    (1, '猫咪', '🐱', 1),
    (2, '狗狗', '🐶', 2),
    (3, '其他', '🐾', 3);

-- 轮播图
INSERT INTO `banner` (`title`, `img`, `link`, `sort`, `status`) VALUES
    ('给流浪的它一个家', 'https://picsum.photos/seed/pet1/1200/400', '/pet', 1, 1),
    ('领养代替购买', 'https://picsum.photos/seed/pet2/1200/400', '/pet', 2, 1),
    ('加入志愿者行列', 'https://picsum.photos/seed/pet3/1200/400', '/volunteer', 3, 1);

-- 公告/资讯
INSERT INTO `article` (`title`, `cover`, `content`, `type`, `author`, `view_count`) VALUES
    ('领养须知', 'https://picsum.photos/seed/notice/400/300',
     '<p>领养前请确认您已做好长期照顾宠物的准备，并具备稳定的居住环境与经济条件。</p>',
     '公告', '平台管理员', 100),
    ('如何正确救助流浪动物', 'https://picsum.photos/seed/rescue/400/300',
     '<p>发现流浪动物时，请先观察其健康状况，避免直接接触可能受伤或受惊的动物，及时通过平台上报救助线索。</p>',
     '科普', '平台管理员', 80);

-- 示例宠物
INSERT INTO `pet` (`name`, `category_id`, `breed`, `gender`, `age`, `vaccine`, `sterilization`, `description`, `cover_img`, `status`, `source`, `review_status`) VALUES
    ('小白', 1, '中华田园猫', '母', '1岁', '已接种', '是', '温顺亲人，已做绝育，适合家庭领养。', 'https://picsum.photos/seed/cat1/400/300', '待领养', 'ADMIN', '已通过'),
    ('旺财', 2, '中华田园犬', '公', '2岁', '已接种', '否', '活泼健康，性格开朗，喜欢散步。', 'https://picsum.photos/seed/dog1/400/300', '待领养', 'ADMIN', '已通过'),
    ('咪咪', 1, '英短', '母', '8个月', '已接种', '否', '可爱粘人，已打疫苗。', 'https://picsum.photos/seed/cat2/400/300', '待领养', 'ADMIN', '已通过');

SET FOREIGN_KEY_CHECKS = 1;
