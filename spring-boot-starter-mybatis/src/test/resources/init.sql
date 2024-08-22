# 全局初始化
set global max_connections=1000;
create database if not exists test;
use test;
create table if not exists t_user(id varchar(64) primary key, first_name varchar(128), last_name varchar(128), create_time timestamp(3), properties varchar(128));
create table if not exists t_activity(activity_id varchar(64) primary key, display_setting varchar(1280), biz_type varchar(128), config_setting varchar(1280));
create table if not exists t_order(id varchar(64) primary key, order_info varchar(1280));
create table if not exists t_dynamodb_type_handler(id varchar(64) primary key, order_info varchar(1280));