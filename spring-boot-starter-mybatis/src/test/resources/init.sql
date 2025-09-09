# 全局初始化
set global max_connections = 1000;
create database if not exists test;
use test;
create table if not exists t_user
(
    id          varchar(64) primary key,
    first_name  varchar(128),
    last_name   varchar(128),
    create_time timestamp(3),
    # 增加一个自动设置的属性，没有体现在实体类中，用于测试 getAutoMappingUnknownColumnBehavior 正常工作
    update_time timestamp(3) default current_timestamp(3) on update current_timestamp(3),
    # 增加一个json属性
    properties  varchar(128)
);
create table if not exists t_activity
(
    activity_id     varchar(64) primary key,
    display_setting varchar(1280),
    biz_type        varchar(128),
    config_setting  varchar(1280)
);
create table if not exists t_order
(
    id         varchar(64) primary key,
    order_info varchar(1280)
);
create table if not exists t_dynamodb_type_handler
(
    id         varchar(64) primary key,
    order_info varchar(1280)
);