-- 创建数据库
CREATE DATABASE IF NOT EXISTS online_image_library;
USE online_image_library;

create table picture
(
    id               bigint auto_increment comment 'id'
        primary key,
    url              varchar(512)                       not null comment '图片 url',
    name             varchar(128)                       not null comment '图片名称',
    introduction     varchar(512)                       null comment '简介',
    category         varchar(64)                        null comment '分类',
    tags             varchar(512)                       null comment '标签（JSON 数组）',
    picSize          bigint                             null comment '图片体积',
    picWidth         int                                null comment '图片宽度',
    picHeight        int                                null comment '图片高度',
    picScale         double                             null comment '图片宽高比例',
    picFormat        varchar(32)                        null comment '图片格式',
    userId           bigint                             not null comment '创建用户 id',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime         datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除',
    reviewStatus     int      default 0                 not null comment '审核状态：0-待审核; 1-通过; 2-拒绝',
    reviewMessage    varchar(512)                       null comment '审核信息',
    reviewerId       bigint                             null comment '审核人 ID',
    reviewTime       datetime                           null comment '审核时间',
    thumbnailUrl     varchar(512)                       null comment '缩略图 url',
    OriginalImageurl varchar(256)                       null comment '原图 url',
    spaceId          bigint   default 0                 null comment '空间 id（为0表示公共空间）'
)
    comment '图片' collate = utf8mb4_unicode_ci;

create index idx_category
    on picture (category);

create index idx_introduction
    on picture (introduction);

create index idx_name
    on picture (name);

create index idx_picture_url
    on picture (url);

create index idx_reviewStatus
    on picture (reviewStatus);

create index idx_spaceId
    on picture (spaceId);

create index idx_tags
    on picture (tags);

create index idx_userId
    on picture (userId);

create table space
(
    id       bigint auto_increment comment 'id'
        primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '所属用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete tinyint default 0 not null comment '是否删除'
)
    comment '空间' collate = utf8mb4_unicode_ci;

create index idx_spaceLevel
    on space (spaceLevel);

create index idx_spaceName
    on space (spaceName);

create index idx_userId
    on space (userId);

create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    constraint uk_userAccount_isDelete
        unique (userAccount, isDelete)
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_userName
    on user (userName);



