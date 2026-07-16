# 创建库
drop database if exists releaseon_app;
drop user if exists 'releaseon_app'@'%';
-- 支持emoji：需要mysql数据库参数： character_set_server=utf8mb4
create database releaseon_app default character set utf8mb4 collate utf8mb4_unicode_ci;
use releaseon_app;
create user 'releaseon_app'@'%' identified by 'releaseon_app123456';
grant all privileges on *.* to 'releaseon_app'@'%';
flush privileges;