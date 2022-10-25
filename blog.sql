/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.39 : Database - sob_blog_system
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`sob_blog_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `sob_blog_system`;

/*Table structure for table `tb_article` */

DROP TABLE IF EXISTS `tb_article`;

CREATE TABLE `tb_article` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `title` varchar(256) NOT NULL COMMENT '标题',
  `user_id` varchar(20) NOT NULL COMMENT '用户ID',
  `category_id` varchar(20) DEFAULT NULL COMMENT '分类ID',
  `content` mediumtext COMMENT '文章内容',
  `type` varchar(1) NOT NULL COMMENT '类型（0表示富文本，1表示markdown）',
  `cover` varchar(1024) DEFAULT NULL COMMENT '封面',
  `state` varchar(1) NOT NULL COMMENT '状态（0表示已发布，1表示草稿，2表示删除）',
  `summary` text COMMENT '摘要',
  `label` varchar(128) DEFAULT NULL COMMENT '标签',
  `view_count` int(11) NOT NULL DEFAULT '0' COMMENT '阅读数量',
  `create_time` datetime NOT NULL COMMENT '发布时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_user_article_on_user_id` (`user_id`),
  KEY `fk_category_article_on_category_id` (`category_id`),
  CONSTRAINT `fk_category_article_on_category_id` FOREIGN KEY (`category_id`) REFERENCES `tb_categories` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_article_on_user_id` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_categories` */

DROP TABLE IF EXISTS `tb_categories`;

CREATE TABLE `tb_categories` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `pinyin` varchar(128) NOT NULL COMMENT '拼音',
  `description` text NOT NULL COMMENT '描述',
  `order` int(11) NOT NULL COMMENT '顺序',
  `status` varchar(1) NOT NULL COMMENT '状态：0表示不使用，1表示正常',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_comment` */

DROP TABLE IF EXISTS `tb_comment`;

CREATE TABLE `tb_comment` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `parent_content` text COMMENT '父内容',
  `article_id` varchar(20) NOT NULL COMMENT '文章ID',
  `content` text NOT NULL COMMENT '评论内容',
  `user_id` varchar(20) NOT NULL COMMENT '评论用户的ID',
  `user_avatar` varchar(1024) DEFAULT NULL COMMENT '评论用户的头像',
  `user_name` varchar(32) DEFAULT NULL COMMENT '评论用户的名称',
  `state` varchar(1) NOT NULL COMMENT '状态（0表示删除，1表示正常）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_user_comment_on_user_id` (`user_id`),
  KEY `fk_article_comment_on_article_id` (`article_id`),
  CONSTRAINT `fk_article_comment_on_article_id` FOREIGN KEY (`article_id`) REFERENCES `tb_article` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_comment_on_user_id` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_daily_view_count` */

DROP TABLE IF EXISTS `tb_daily_view_count`;

CREATE TABLE `tb_daily_view_count` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `view_count` int(11) NOT NULL DEFAULT '0' COMMENT '每天浏览量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_friends` */

DROP TABLE IF EXISTS `tb_friends`;

CREATE TABLE `tb_friends` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `name` varchar(64) NOT NULL COMMENT '友情链接名称',
  `logo` varchar(1024) NOT NULL COMMENT '友情链接logo',
  `url` varchar(1024) NOT NULL COMMENT '友情链接',
  `order` int(11) NOT NULL DEFAULT '0' COMMENT '顺序',
  `state` varchar(1) NOT NULL COMMENT '友情链接状态:0表示不可用，1表示正常',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_images` */

DROP TABLE IF EXISTS `tb_images`;

CREATE TABLE `tb_images` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `user_id` varchar(20) NOT NULL COMMENT '用户ID',
  `url` varchar(1024) NOT NULL COMMENT '路径',
  `path` varchar(512) NOT NULL COMMENT '存储路径',
  `content_type` varchar(32) NOT NULL COMMENT '图片类型',
  `name` varchar(100) NOT NULL COMMENT '原名称',
  `state` varchar(1) NOT NULL COMMENT '状态（0表示删除，1表正常）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_user_images_on_user_id` (`user_id`),
  CONSTRAINT `fk_user_images_on_user_id` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_labels` */

DROP TABLE IF EXISTS `tb_labels`;

CREATE TABLE `tb_labels` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `name` varchar(32) NOT NULL COMMENT '标签名称',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_looper` */

DROP TABLE IF EXISTS `tb_looper`;

CREATE TABLE `tb_looper` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `title` varchar(128) NOT NULL COMMENT '轮播图标题',
  `order` int(11) NOT NULL DEFAULT '0' COMMENT '顺序',
  `state` varchar(1) NOT NULL COMMENT '状态：0表示不可用，1表示正常',
  `target_url` varchar(1024) DEFAULT NULL COMMENT '目标URL',
  `image_url` varchar(2014) NOT NULL COMMENT '图片地址',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_refresh_token` */

DROP TABLE IF EXISTS `tb_refresh_token`;

CREATE TABLE `tb_refresh_token` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `refresh_token` text NOT NULL COMMENT '刷新的ID',
  `user_id` varchar(20) NOT NULL COMMENT '用户ID',
  `mobile_token_key` varchar(34) DEFAULT NULL COMMENT '移动端的token_key',
  `token_key` varchar(34) DEFAULT NULL COMMENT 'token的key',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_settings` */

DROP TABLE IF EXISTS `tb_settings`;

CREATE TABLE `tb_settings` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `key` varchar(32) NOT NULL COMMENT '键',
  `value` varchar(512) NOT NULL COMMENT '值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `tb_user` */

DROP TABLE IF EXISTS `tb_user`;

CREATE TABLE `tb_user` (
  `id` varchar(20) NOT NULL COMMENT 'ID',
  `user_name` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `roles` varchar(100) NOT NULL COMMENT '角色',
  `avatar` varchar(1024) NOT NULL COMMENT '头像地址',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱地址',
  `sign` varchar(100) DEFAULT NULL COMMENT '签名',
  `state` varchar(1) NOT NULL COMMENT '状态：0表示删除，1表示正常',
  `reg_ip` varchar(32) NOT NULL COMMENT '注册ip',
  `login_ip` varchar(32) NOT NULL COMMENT '登录Ip',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
