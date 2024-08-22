create database if not exists `test`;
use test;
CREATE TABLE if not exists `t_common_mq_fail_log`
(
    `id`          varchar(40),
    `topic`       varchar(145),
    `hash_key`    varchar(145),
    `trace_id`    varchar(50),
    `body`        text,
    `send_config` varchar(245),
    `retry_num`   int,
    `send_status` int,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);