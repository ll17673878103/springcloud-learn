-- ========================================
-- Seata 分布式事务实战 - 数据库初始化脚本
-- ========================================

USE springcloud;

-- 1. 用户表（存储用户余额）
CREATE TABLE IF NOT EXISTS `user` (
  `id`        BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username`  VARCHAR(50) NOT NULL COMMENT '用户名',
  `balance`   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '账户余额'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 2. 订单表（存储订单信息）
CREATE TABLE IF NOT EXISTS `order` (
  `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
  `product`    VARCHAR(100) NOT NULL COMMENT '商品名称',
  `amount`     DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status`     VARCHAR(20)  NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- 3. undo_log 表（Seata AT 模式回滚日志表，必需！）
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL COMMENT '分支事务ID',
  `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context`       VARCHAR(128) NOT NULL COMMENT '上下文',
  `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息（前镜像+后镜像）',
  `log_status`    INT(11)      NOT NULL COMMENT '日志状态',
  `log_created`   DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified`  DATETIME(6)  NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = 'AT模式回滚日志表';

-- 4. 插入测试数据
INSERT INTO `user` (id, username, balance) VALUES (1, '张三', 1000.00);

-- 查看初始化结果
SELECT * FROM `user`;
SELECT * FROM `order`;
SELECT * FROM `undo_log`;
