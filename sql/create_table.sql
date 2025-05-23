-- 创建数据库
CREATE DATABASE IF NOT EXISTS online_image_library;
USE online_image_library;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
                                    id BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
                                    userAccount VARCHAR(256) NOT NULL COMMENT '账号',
                                    userPassword VARCHAR(512) NOT NULL COMMENT '密码',
                                    userName VARCHAR(256) NULL COMMENT '用户昵称',
                                    userAvatar VARCHAR(1024) NULL COMMENT '用户头像',
                                    userProfile VARCHAR(512) NULL COMMENT '用户简介',
                                    userRole VARCHAR(256) DEFAULT 'user' NOT NULL COMMENT '用户角色：user/admin',
                                    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                    editTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
                                    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                                    UNIQUE KEY uk_userAccount_isDelete (userAccount, isDelete),
                                    INDEX idx_userName (userName)
) COMMENT '用户' COLLATE utf8mb4_unicode_ci;

-- 图片表（包含所有后续添加的字段）
CREATE TABLE IF NOT EXISTS picture (
                                       id BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
                                       url VARCHAR(256) NOT NULL COMMENT '图片 url',
                                       thumbnailUrl VARCHAR(256) NULL COMMENT '缩略图 url',
                                       OriginalImageurl VARCHAR(256) NULL COMMENT '原图 url',
                                       name VARCHAR(128) NOT NULL COMMENT '图片名称',
                                       introduction VARCHAR(512) NULL COMMENT '简介',
                                       category VARCHAR(64) NULL COMMENT '分类',
                                       tags VARCHAR(256) NULL COMMENT '标签（JSON 数组）',
                                       picSize BIGINT NULL COMMENT '图片体积',
                                       picWidth INT NULL COMMENT '图片宽度',
                                       picHeight INT NULL COMMENT '图片高度',
                                       picScale DOUBLE NULL COMMENT '图片宽高比例',
                                       picFormat VARCHAR(32) NULL COMMENT '图片格式',
                                       reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
                                       reviewMessage VARCHAR(256) NULL COMMENT '审核信息',
                                       reviewerId BIGINT NULL COMMENT '审核人 ID',
                                       reviewTime DATETIME NULL COMMENT '审核时间',
                                       userId BIGINT NOT NULL COMMENT '创建用户 id',
                                       createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                       editTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
                                       updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                                       INDEX idx_url (url),
                                       INDEX idx_name (name),
                                       INDEX idx_introduction (introduction),
                                       INDEX idx_category (category),
                                       INDEX idx_tags (tags),
                                       INDEX idx_userId (userId),
                                       INDEX idx_reviewStatus (reviewStatus)
) COMMENT '图片' COLLATE utf8mb4_unicode_ci;
