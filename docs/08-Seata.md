# 08-Seata 分布式事务

## 本阶段目标

- 理解为什么微服务需要分布式事务
- 掌握 Seata 的核心概念（TC、TM、RM）
- 理解 Seata AT 模式的工作原理（undo_log 回滚日志）
- 了解 Seata 的四种事务模式（AT、TCC、Saga、XA）
- 掌握 Seata Server 搭建与 Nacos 集成
- 掌握客户端集成方法（依赖 + 配置 + @GlobalTransactional）

---

## 为什么需要分布式事务？

### 从我们的项目说起

当前的调用链路：

```
用户请求 → Gateway(8080) → order-service(8082) → (Feign) → user-service(8081)
```

在 `OrderController.createOrder()` 中：

```java
// 1. 调用 user-service 获取用户信息
Map<String, Object> userInfo = userClient.getUser(userId);

// 2. 创建订单
return Map.of("orderId", System.currentTimeMillis(), ...);
```

目前是模拟数据，没有真实数据库操作。但想象一下真实业务场景：

```
下单业务（涉及两个服务 + 两个数据库）：

order-service                           user-service
    │                                       │
    ├─ 1. 扣减用户余额 ────Feign──────→  UPDATE user SET balance = balance - 100
    │                                       │  ✅ 余额扣了！
    │                                       
    ├─ 2. 创建订单                          
    │   INSERT INTO order ...               
    │   💥 但是这里出错了！抛异常！          
    │                                       
    └─ ❌ 订单没创建成功                    
                                        😱 但是用户的钱已经扣了！
```

**问题出现了**：用户余额扣了，但订单没创建。钱凭空消失了。

这就是**分布式事务问题**。

### 单体时代 vs 微服务时代

**单体时代**（一个数据库，一个 `@Transactional` 搞定）：

```java
@Transactional  // 一个事务，全成功或全回滚
public void createOrder(Long userId, BigDecimal amount) {
    userDao.deductBalance(userId, amount);   // 扣余额
    orderDao.createOrder(userId, amount);     // 创建订单
    // 任何一步失败，全部回滚，数据一致性有保障 ✅
}
```

**微服务时代**（多个服务，多个数据库）：

```java
// order-service 中
public void createOrder(Long userId, BigDecimal amount) {
    userClient.deductBalance(userId, amount);  // HTTP调用 user-service → 操作 user_db
    orderDao.createOrder(userId, amount);       // 本地操作 → 操作 order_db
    
    // 问题：两个操作在不同的数据库！
    // @Transactional 只能管本地数据库
    // Feign 调用成功后，本地如果失败 → user-service 的操作无法自动回滚！
}
```

> 就像一首曲子由两个人各拿一半乐谱来演奏——如果一个人中途停了，另一个人不知道，曲子就乱了。

### 分布式事务的三种经典问题

| 场景 | 问题 | 后果 |
|------|------|------|
| **创建失败** | 远程调用成功，本地失败 | 扣了钱，没订单 |
| **远程失败** | 本地成功，远程调用超时/失败 | 有订单，没扣钱 |
| **网络分区** | 调用发出去了，但不知道结果 | 不知道该不该回滚 |

---

## Seata 是什么？

**Seata**（Simple Extensible Autonomous Transaction Architecture）是阿里开源的分布式事务解决方案。

它的核心思想是：**把分布式事务的复杂性像乐团指挥一样统一管理**。

### 三个核心角色

| 角色 | 英文 | 职责 | 比喻 |
|------|------|------|------|
| **TC** | Transaction Coordinator | 维护全局事务状态，指挥提交/回滚 | 乐团指挥 |
| **TM** | Transaction Manager | 发起全局事务、提交或回滚 | 首席演奏者 |
| **RM** | Resource Manager | 管理本地资源的分支事务 | 各声部演奏者 |

### 架构图

```
                    ┌─────────────────┐
                    │   Seata Server   │
                    │   (TC 事务协调者)  │
                    │   统一指挥全局事务  │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
        ┌─────┴─────┐  ┌────┴─────┐  ┌─────┴─────┐
        │order-service│  │user-service│  │stock-service│
        │  (TM + RM) │  │  (RM)    │  │  (RM)    │
        │  发起事务    │  │  分支事务  │  │  分支事务  │
        └───────────┘  └──────────┘  └───────────┘
```

### 全局事务流程

```
1. TM 向 TC 申请开启全局事务 → TC 返回 XID（全局事务ID）
2. TM 携带 XID 发起业务调用
3. 各 RM 向 TC 注册分支事务，绑定到 XID
4. 各 RM 执行本地 SQL，生成 undo_log
5. TM 根据执行结果，向 TC 发送提交或回滚
6. TC 指挥所有 RM 提交或回滚
```

---

## Seata 四种事务模式

| 模式 | 一句话概括 | 适用场景 | 学习优先级 |
|------|-----------|---------|-----------|
| **AT** | 无侵入，自动回滚（像增强版 @Transactional） | 大多数业务场景 | 🔥 **重点学** |
| **TCC** | 手动编写 Try/Confirm/Cancel 三个方法 | 需要精细化控制 | 🟡 了解 |
| **Saga** | 长事务，正向+补偿 | 业务流程很长 | 🟡 了解 |
| **XA** | 数据库层面的两阶段提交 | 强一致性要求高 | ⚪ 知道即可 |

---

## AT 模式详解（重点）

### 核心思想

AT 模式是**无侵入式**的分布式事务解决方案，只需要：
1. 添加 `@GlobalTransactional` 注解
2. 每个数据库创建 `undo_log` 表

Seata 会自动帮你处理分布式事务的回滚。

### 关键机制：DataSourceProxy（数据源代理）

AT 模式的"魔法"来自 **DataSourceProxy**——Seata 会在底层代理你的数据源，拦截所有 JDBC 操作：

```
你的代码                     Seata 的代理层                  真实数据库
   │                            │                            │
   ├─ executeUpdate(SQL)        │                            │
   │         ┌──────────────────┤                            │
   │         │ ① 解析 SQL 语义   │                            │
   │         │ ② 查询前镜像      ├─ SELECT ... FOR UPDATE ──→ │
   │         │ ③ 执行业务 SQL    ├─ UPDATE ... ─────────────→│
   │         │ ④ 查询后镜像      ├─ SELECT ... ─────────────→│
   │         │ ⑤ 写入 undo_log  ├─ INSERT undo_log ────────→│
   │         │ ⑥ 提交本地事务    ├─ COMMIT ────────────────→│
   │         └──────────────────┤                            │
   │                            │                            │
```

> 这一切对你完全透明——你的业务代码不需要做任何改动。

### 一阶段详细流程（核心！）

以 `UPDATE user SET balance = balance - 100 WHERE id = 1` 为例：

**第①步：解析 SQL**

Seata 的 DataSourceProxy 拦截到你执行的 UPDATE 语句，解析出：
- 表名：`user`
- 条件：`id = 1`
- 操作类型：UPDATE

**第②步：查询前镜像（Before Image）**

Seata 自动构造一条 SELECT 语句，查询修改前的数据：

```sql
SELECT id, name, balance FROM user WHERE id = 1;
-- 结果：{ id: 1, name: "张三", balance: 500.00 }
```

这就是"前镜像"——记录数据修改前的样子。

**第③步：执行业务 SQL**

执行你原始的业务 SQL：

```sql
UPDATE user SET balance = balance - 100 WHERE id = 1;
-- 执行后 balance 变成 400.00
```

**第④步：查询后镜像（After Image）**

再次查询，获取修改后的数据：

```sql
SELECT id, name, balance FROM user WHERE id = 1;
-- 结果：{ id: 1, name: "张三", balance: 400.00 }
```

这就是"后镜像"——记录数据修改后的样子。

**第⑤步：生成 undo_log**

将前镜像 + 后镜像打包成 JSON，插入到 `undo_log` 表：

```json
{
  "sqlType": "UPDATE",
  "tableName": "user",
  "beforeImage": {
    "rows": [{ "fields": [
      { "name": "id", "value": 1 },
      { "name": "balance", "value": 500.00 }
    ]}]
  },
  "afterImage": {
    "rows": [{ "fields": [
      { "name": "id", "value": 1 },
      { "name": "balance", "value": 400.00 }
    ]}]
  }
}
```

**第⑥步：提交本地事务**

业务 SQL + undo_log 的插入在**同一个本地事务**中提交。这样保证了：
- 要么业务 SQL 和 undo_log 都成功
- 要么都失败，数据一致性不会有问题

> ⚠️ **AT 模式 vs XA 模式的关键区别**：
> - XA 模式：一阶段不提交，持有数据库锁直到全局事务结束
> - AT 模式：一阶段**立即提交**本地事务，释放数据库锁
> - AT 模式用"全局锁"代替数据库锁，性能更好

### 二阶段详细流程

#### 情况A：全局提交（全部成功）

```
TM 检测所有分支成功 → 通知 TC → TC 通知所有 RM 提交

各 RM 收到提交指令后：
  ├─ 异步删除 undo_log 记录（因为已经提交了，不需要回滚了）
  └─ 释放全局锁

整个过程非常快！因为本地事务已经提交了，只需要清理 undo_log。
```

#### 情况B：全局回滚（有失败）

```
TM 检测到异常 → 通知 TC → TC 通知所有 RM 回滚

各 RM 收到回滚指令后：
  ├─ ① 读取 undo_log
  ├─ ② 校验脏写：比较 afterImage 和当前数据库数据
  │      ├─ 如果一致 → 没有脏写，可以安全回滚
  │      └─ 如果不一致 → 有人改了数据！需要人工干预
  ├─ ③ 根据 beforeImage 生成反向 SQL
  │      beforeImage: { balance: 500.00 }
  │      → UPDATE user SET balance = 500.00 WHERE id = 1
  ├─ ④ 执行反向 SQL
  └─ ⑤ 删除 undo_log
```

**脏写校验**很重要：如果在我们的分布式事务期间，有其他事务修改了同一行数据，Seata 检测到后不会盲目回滚，而是需要人工介入——这保证了数据安全。

### 全局锁机制

AT 模式一阶段就提交了本地事务，那怎么防止其他事务修改数据导致无法回滚？答案是**全局锁**。

```
服务A（扣余额）                    Seata TC
    │                                │
    ├─ 执行 UPDATE user ...          │
    ├─ 申请全局锁（user 表, id=1）──→│ TC 记录：XID:xxx 锁住了 user:id=1
    ├─ 全局锁获取成功 ←──────────────│
    ├─ 提交本地事务                   │
    │                                │
    │    服务B 也想改 user id=1       │
    │        ├─ 申请全局锁 ─────────→│ 发现已被锁住
    │        ├─ 等待/重试              │
    │        └─ 超时后获取失败         │
    │                                │
    ├─ 全局事务结束，释放全局锁 ────→│
    │                                │
    │    服务B 重新申请全局锁 ────────→│ 获取成功
```

> 全局锁是 Seata TC 端维护的，不是数据库锁。
> 本地事务已经提交释放了数据库锁，但全局锁保证在分布式事务期间，其他 Seata 事务不能修改同一行数据。

### undo_log 表结构

每个参与分布式事务的数据库都需要创建这张表：

```sql
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL COMMENT '分支事务ID',
  `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context`       VARCHAR(128) NOT NULL COMMENT '上下文',
  `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息（前镜像+后镜像的JSON）',
  `log_status`    INT(11)      NOT NULL COMMENT '日志状态（0=正常, 1=防悬挂）',
  `log_created`   DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified`  DATETIME(6)  NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT模式回滚日志表';
```

| 字段 | 说明 |
|------|------|
| `branch_id` | 分支事务的唯一ID |
| `xid` | 全局事务ID（同一个分布式事务中所有分支共享） |
| `rollback_info` | 回滚信息（前镜像+后镜像的 JSON，压缩后存储） |
| `log_status` | 0=正常可回滚，1=防悬挂标记（已回滚过） |

> undo_log 表的 SQL 脚本在 Seata 官方 GitHub 的 `script/client/at/db/mysql.sql` 目录下。

### 完整时序图

```
 TM (order-service)              TC (Seata Server)           RM (user-service)
     │                                │                            │
     ├─ @GlobalTransactional         │                            │
     ├─ 开启全局事务 ────────────────→│                            │
     │   返回 XID ←──────────────────│                            │
     │                                │                            │
     ├─ Feign 调用（携带 XID）────────│──────────────────────────→│
     │                                │                            ├─ 注册分支事务 ──→│
     │                                │                            ├─ 解析 SQL
     │                                │                            ├─ 查询前镜像
     │                                │                            ├─ 执行业务 SQL
     │                                │                            ├─ 查询后镜像
     │                                │                            ├─ 申请全局锁 ────→│
     │                                │                            ├─ 写入 undo_log
     │                                │                            ├─ 提交本地事务
     │                                │                            ├─ 返回结果 ←────│
     │                                │                            │
     ├─ 本地操作（同理）               │                            │
     │   ...                          │                            │
     │                                │                            │
     ├─ 全部成功，通知 TC 提交 ──────→│                            │
     │                                ├─ 异步清理 undo_log ────────→│
     │                                ├─ 释放全局锁                 │
     │   返回结果                      │                            │
     │                                │                            │
     ═══════════════════ 或者有异常 ══════════════════════════════
     │                                │                            │
     ├─ 有异常，通知 TC 回滚 ────────→│                            │
     │                                ├─ 根据 undo_log 回滚 ───────→│
     │                                │   (校验脏写→反向SQL→清理)    │
     │                                ├─ 释放全局锁                 │
     │   抛出异常                      │                            │
```

---

## 其他模式简述

### TCC 模式

手动编写三个方法：

```java
// Try：预留资源（冻结余额）
public boolean tryDeduct(Long userId, BigDecimal amount) {
    // UPDATE user SET frozen = frozen + amount, balance = balance - amount WHERE id = ?
}

// Confirm：确认扣款（消耗冻结金额）
public boolean confirmDeduct(Long userId, BigDecimal amount) {
    // UPDATE user SET frozen = frozen - amount WHERE id = ?
}

// Cancel：取消扣款（恢复余额）
public boolean cancelDeduct(Long userId, BigDecimal amount) {
    // UPDATE user SET balance = balance + amount, frozen = frozen - amount WHERE id = ?
}
```

| 阶段 | 作用 | 比喻 |
|------|------|------|
| Try | 预留资源 | 先把座位占住 |
| Confirm | 确认使用 | 正式入座 |
| Cancel | 释放资源 | 退座位 |

### Saga 模式

```
正向执行：                补偿回滚：
A → B → C → D            A⁻¹ ← B⁻¹ ← C⁻¹ ← D⁻¹
（每步成功继续）            （从失败点往前反向补偿）
```

- 适用于**长事务**（跨天、跨系统的业务流程）
- 每个步骤需要定义对应的补偿操作

### XA 模式

- 使用数据库原生的两阶段提交协议
- 强一致性，但性能较差（持有数据库锁时间长）
- 适用于对一致性要求极高的金融场景

---

## 版本说明

### 当前项目版本对应

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.0 |
| Spring Cloud | 2023.0.0 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| **Seata** | **2.0.0**（由 SCA BOM 管理） |
| Nacos | 已部署（v2.2.3） |

### Seata 依赖（由 Spring Cloud Alibaba BOM 管理版本）

```xml
<!-- 父 POM 已有的依赖管理 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>2023.0.1.0</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<!-- 子模块只需要引入，不需要指定版本 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

---

## Seata Server 搭建（注册到 Nacos）

### 整体步骤概览

```
1. 下载 Seata Server 2.0.0
2. 创建 Seata 数据库（存储事务日志）
3. 修改 Seata Server 配置（application.yml）
4. 在 Nacos 中添加 seataServer.properties 配置
5. 启动 Seata Server
6. 验证注册到 Nacos
```

### 步骤一：下载 Seata Server

```powershell
# 下载地址（选择 2.0.0 版本）
# GitHub: https://github.com/apache/incubator-seata/releases/tag/v2.0.0
# 下载 seata-server-2.0.0.zip 或 .tar.gz

# 解压到任意目录，例如
# D:\seata-server-2.0.0
```

> ⚠️ 我们的 Spring Cloud Alibaba 2023.0.1.0 对应 Seata 2.0.0，版本必须匹配。

解压后目录结构：

```
seata-server-2.0.0/
├── bin/
│   ├── seata-server.sh       # Linux/Mac 启动脚本
│   └── seata-server.bat      # Windows 启动脚本
├── conf/
│   ├── application.yml       # ⭐ 主配置文件（Seata 2.0 起用 yml，不再用 registry.conf）
│   └── application.example.yml  # 配置示例
├── lib/                      # 依赖 jar
└── logs/                     # 日志
```

### 步骤二：创建 Seata 数据库

Seata Server 需要数据库来存储全局事务会话信息（global_table、branch_table、lock_table）。

建表 SQL 在 `seata/script/server/db/mysql.sql`（如果没有 script 目录，从 GitHub 下载）。

```sql
-- 创建 seata 数据库
CREATE DATABASE seata DEFAULT CHARACTER SET utf8mb4;
USE seata;

-- 以下是 Seata 2.0.0 的建表 SQL（简化版，完整版参考官方 script）

-- 全局事务表
CREATE TABLE IF NOT EXISTS `global_table` (
  `xid`                       VARCHAR(128) NOT NULL,
  `transaction_id`            BIGINT,
  `status`                    TINYINT      NOT NULL,
  `application_id`            VARCHAR(32),
  `transaction_service_group` VARCHAR(32),
  `transaction_name`          VARCHAR(128),
  `timeout`                   INT,
  `begin_time`                BIGINT,
  `application_data`          VARCHAR(2000),
  `gmt_create`                DATETIME,
  `gmt_modified`              DATETIME,
  PRIMARY KEY (`xid`),
  KEY `idx_status_gmt_modified` (`status` , `gmt_modified`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 分支事务表
CREATE TABLE IF NOT EXISTS `branch_table` (
  `branch_id`         BIGINT       NOT NULL,
  `xid`               VARCHAR(128) NOT NULL,
  `transaction_id`    BIGINT,
  `resource_group_id` VARCHAR(32),
  `resource_id`       VARCHAR(256),
  `branch_type`       VARCHAR(8),
  `status`            TINYINT,
  `client_id`         VARCHAR(64),
  `application_data`  VARCHAR(2000),
  `gmt_create`        DATETIME(6),
  `gmt_modified`      DATETIME(6),
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 全局锁表
CREATE TABLE IF NOT EXISTS `lock_table` (
  `row_key`        VARCHAR(128) NOT NULL,
  `xid`            VARCHAR(128),
  `transaction_id` BIGINT,
  `branch_id`      BIGINT       NOT NULL,
  `resource_id`    VARCHAR(256),
  `table_name`     VARCHAR(32),
  `pk`             VARCHAR(36),
  `status`         TINYINT      NOT NULL DEFAULT '0',
  `gmt_create`     DATETIME,
  `gmt_modified`   DATETIME,
  PRIMARY KEY (`row_key`),
  KEY `idx_status` (`status`),
  KEY `idx_branch_id` (`branch_id`),
  KEY `idx_xid_and_branch_id` (`xid` , `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

| 表名 | 作用 |
|------|------|
| `global_table` | 记录全局事务的状态（开始/提交/回滚） |
| `branch_table` | 记录每个分支事务的信息 |
| `lock_table` | 记录全局锁（哪行数据被哪个事务锁住） |

### 步骤三：修改 Seata Server 配置

编辑 `conf/application.yml`：

```yaml
seata:
  server:
    service-port: 8091          # Seata Server 服务端口（客户端连接用这个）

  # 注册中心配置：注册到 Nacos
  registry:
    type: nacos
    nacos:
      application: seata-server     # 注册到 Nacos 的服务名
      server-addr: <YOUR_NACOS_IP>:8848   # ⭐ 改成你的 Nacos 地址
      group: SEATA_GROUP
      namespace: a4444b64-9f98-4af6-882c-c3d265af4987   # ⭐ 改成你的 Nacos namespace
      cluster: default
      # username: nacos              # 如果 Nacos 开启了认证
      # password: nacos

  # 配置中心：从 Nacos 读取配置
  config:
    type: nacos
    nacos:
      server-addr: 101.43.103.32:8848   # ⭐ 同上
      group: SEATA_GROUP
      namespace: a4444b64-9f98-4af6-882c-c3d265af4987   # ⭐ 同上
      data-id: seataServer.properties   # Nacos 中的配置 Data ID
      # username: nacos
      # password: nacos

  # 存储模式
  store:
    mode: db                           # 使用数据库存储（生产推荐）
    db:
      datasource: druid
      db-type: mysql
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://<YOUR_MYSQL_IP>:3306/seata?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai   # ⭐ 改成你的 MySQL
      user: root
      password: luolei0525              # ⭐ 改成你的密码
      min-conn: 10
      max-conn: 100
```

> ⚠️ **重要提示**：Seata 2.0 起不再使用 `registry.conf` 和 `file.conf`，统一用 `application.yml`。

### 步骤四：在 Nacos 中添加 seataServer.properties

打开 Nacos 控制台（http://<YOUR_NACOS_IP>:8848/nacos），在对应的 namespace 下新建配置：

- **Data ID**：`seataServer.properties`
- **Group**：`SEATA_GROUP`
- **配置格式**：`Properties`
- **配置内容**：

```properties
# 事务分组 → 集群映射
# 客户端配置的 tx-service-group 的值会通过这个映射找到对应的集群
service.vgroupMapping.default_tx_group=default

# 默认集群的 TC 地址（注册到 Nacos 后可以不配，但建议保留作兜底）
service.default.grouplist=127.0.0.1:8091

# 存储模式
store.mode=db
store.lock.mode=db
store.session.mode=db

# 数据库存储配置
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://<YOUR_MYSQL_IP>:3306/seata?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
store.db.user=root
store.db.password=luolei0525
```

> 💡 事务分组映射 `service.vgroupMapping.default_tx_group=default` 很重要：
> - 客户端配置的 `seata.tx-service-group=default_tx_group`
> - Seata 通过这个映射找到 `default` 集群
> - 再从 Nacos 注册中心找到 `default` 集群的 TC 地址

### 步骤五：启动 Seata Server

```powershell
# Windows
cd D:\seata-server-2.0.0\bin
seata-server.bat

# Linux/Mac
sh bin/seata-server.sh
```

启动成功后日志会显示：

```
Server started, listen port: 8091
```

### 步骤六：验证注册到 Nacos

打开 Nacos 控制台 → 服务列表，应该能看到：

```
服务名: seata-server
分组:   SEATA_GROUP
集群:   default
实例:   你的IP:8091
```

> 如果看不到，检查 namespace 是否一致、网络是否通畅。

### Seata Server 端口说明

| 端口 | 作用 |
|------|------|
| **8091** | TC 服务端口（客户端连接用） |
| **7091** | Seata 控制台端口（浏览器访问，可查看事务状态） |

浏览器访问 `http://localhost:7091` 可以看到 Seata 控制台。

---

## 客户端集成实战

### 集成步骤概览

```
1. 引入 Seata 依赖（order-service、user-service 都要加）
2. 创建 undo_log 表（每个业务的数据库都要建）
3. 配置 application.yml（连接 Seata Server）
4. 业务代码添加 @GlobalTransactional 注解
5. 验证分布式事务
```

### 步骤一：引入 Seata 依赖

在每个参与分布式事务的微服务的 `pom.xml` 中添加：

```xml
<!-- Seata（分布式事务） -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

因为父 POM 已经通过 `spring-cloud-alibaba-dependencies` 管理了版本，所以**不需要指定版本号**。

> ⚠️ **注意**：`spring-cloud-starter-alibaba-seata` 内部已经集成了 `seata-spring-boot-starter` 和 `seata-all`，并且实现了 XID 跨服务传递（通过 Feign 拦截器自动传递全局事务ID）。不需要额外引入 `io.seata:seata-spring-boot-starter`。

### 步骤二：创建 undo_log 表

在**每个参与分布式事务的微服务的数据库**中执行：

```sql
-- 在 order-service 的数据库中执行
-- 在 user-service 的数据库中执行
-- （如果共用一个数据库，执行一次即可）

CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL COMMENT '分支事务ID',
  `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context`       VARCHAR(128) NOT NULL COMMENT '上下文',
  `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息（前镜像+后镜像）',
  `log_status`    INT(11)      NOT NULL COMMENT '日志状态',
  `log_created`   DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified`  DATETIME(6)  NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT模式回滚日志表';
```

> 我们项目目前 user-service 连的是 `springcloud` 数据库（`<YOUR_MYSQL_IP>:3306`），在这个库里建就行。

### 步骤三：配置 application.yml

在每个参与分布式事务的微服务的 `application.yml` 中添加 Seata 配置：

```yaml
# ===== Seata 分布式事务配置 =====
seata:
  enabled: true
  application-id: ${spring.application.name}     # 当前服务名
  tx-service-group: default_tx_group              # 事务分组（必须与 Nacos 中的映射对应）
  
  # 注册中心：从 Nacos 发现 Seata Server
  registry:
    type: nacos
    nacos:
      application: seata-server                    # Seata Server 注册到 Nacos 的服务名
      server-addr: 101.43.103.32:8848              # ⭐ Nacos 地址
      group: SEATA_GROUP
      namespace: a4444b64-9f98-4af6-882c-c3d265af4987   # ⭐ Nacos namespace
      # username: nacos
      # password: nacos
  
  # 配置中心：从 Nacos 读取 Seata 配置
  config:
    type: nacos
    nacos:
      server-addr: 101.43.103.32:8848
      group: SEATA_GROUP
      namespace: a4444b64-9f98-4af6-882c-c3d265af4987
      data-id: seataServer.properties
```

**配置对应关系（很重要）**：

```
客户端配置                     Nacos 中的配置
─────────────────────────────────────────────────
tx-service-group               service.vgroupMapping
= default_tx_group             .default_tx_group=default
     │                              │
     └────── 映射到集群名 ──────────→ default
                                        │
registry.nacos                          │
.application=seata-server               │
     │                                  │
     └── 从 Nacos 找到 seata-server ────┘  → TC 地址
```

> 💡 简单说：客户端配 `tx-service-group=default_tx_group`，Nacos 里配 `service.vgroupMapping.default_tx_group=default`，两者对应上就能找到 Seata Server。

### 步骤四：业务代码添加 @GlobalTransactional

在**发起分布式事务的服务**（TM 端）的方法上添加注解：

```java
// OrderController.java 或 OrderService.java
import io.seata.spring.annotation.GlobalTransactional;

@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
@GetMapping("/create")
public Map<String, Object> createOrder(
        @RequestParam("userId") Long userId,
        HttpServletRequest request) {

    log.info("创建订单开始，XID: {}", RootContext.getXID());  // 可以打印全局事务ID

    // 1. 远程调用 user-service（RM 分支事务）
    Map<String, Object> userInfo = userClient.getUser(userId);

    // 2. 本地创建订单（本地分支事务）
    // orderDao.createOrder(...);

    // 如果任何一步抛异常，Seata 自动回滚所有分支事务
    return Map.of("code", 200, "message", "订单创建成功");
}
```

**要点**：
- `@GlobalTransactional` 加在 **TM 端**（发起方，通常是 order-service）
- **RM 端**（参与方，如 user-service）不需要加这个注解，只要它也配了 Seata 客户端
- `RootContext.getXID()` 可以获取当前全局事务ID，方便调试

### 步骤五：验证分布式事务

**启动顺序**：

```
1. Nacos（已运行）
2. Seata Server（刚搭好的）
3. user-service
4. order-service
```

---

## 分布式事务实战场景

### 场景：扣款 + 下单

```
用户下单 → 扣减余额 → 创建订单（涉及 order-service 和 user-service）
```

### 1. user-service 准备数据表

```sql
-- 在 springcloud 数据库中执行
CREATE TABLE IF NOT EXISTS `user` (
  `id`        BIGINT      PRIMARY KEY AUTO_INCREMENT,
  `username`  VARCHAR(50) NOT NULL COMMENT '用户名',
  `balance`   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '账户余额'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 插入测试数据
INSERT INTO `user` (id, username, balance) VALUES (1, '张三', 1000.00);

-- 创建 undo_log 表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL,
  `xid`           VARCHAR(128) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME(6)  NOT NULL,
  `log_modified`  DATETIME(6)  NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4;
```

### 2. order-service 准备数据表

```sql
-- 在 order-service 的数据库中执行（假设用同一个数据库，或单独建一个 order_db）
CREATE TABLE IF NOT EXISTS `order` (
  `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
  `product`    VARCHAR(100) NOT NULL COMMENT '商品名称',
  `amount`     DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status`     VARCHAR(20)  NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 创建 undo_log 表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL,
  `xid`           VARCHAR(128) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME(6)  NOT NULL,
  `log_modified`  DATETIME(6)  NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4;
```

### 3. user-service 新增扣款接口

```java
// UserController.java
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 扣减余额（RM 分支事务）
     */
    @GetMapping("/deduct")
    public Map<String, Object> deductBalance(@RequestParam Long userId, 
                                             @RequestParam BigDecimal amount) {
        log.info("扣款开始：userId={}, amount={}", userId, amount);
        log.info("当前 XID: {}", RootContext.getXID());
        
        // 先查询余额
        BigDecimal before = jdbcTemplate.queryForObject(
            "SELECT balance FROM user WHERE id = ?", BigDecimal.class, userId);
        
        if (before == null || before.compareTo(amount) < 0) {
            throw new RuntimeException("余额不足！");
        }
        
        // 扣款
        jdbcTemplate.update("UPDATE user SET balance = balance - ? WHERE id = ?", 
                           amount, userId);
        
        BigDecimal after = jdbcTemplate.queryForObject(
            "SELECT balance FROM user WHERE id = ?", BigDecimal.class, userId);
        
        log.info("扣款完成：before={}, after={}", before, after);
        return Map.of("code", 200, "message", "扣款成功", "before", before, "after", after);
    }
}
```

### 4. order-service 新增下单接口（TM 发起方）

```java
// OrderController.java
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserClient userClient;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建订单（TM 全局事务发起方）
     */
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    @GetMapping("/create")
    public Map<String, Object> createOrder(
            @RequestParam Long userId,
            @RequestParam String product,
            @RequestParam BigDecimal amount) {
        
        log.info("========== 创建订单开始 ==========");
        log.info("全局事务 XID: {}", RootContext.getXID());
        log.info("参数: userId={}, product={}, amount={}", userId, product, amount);
        
        // 1. 远程调用 user-service 扣款（RM 分支事务）
        log.info("第一步：调用 user-service 扣款...");
        Map<String, Object> deductResult = userClient.deductBalance(userId, amount);
        log.info("扣款结果: {}", deductResult);
        
        // 2. 本地创建订单（本地分支事务）
        log.info("第二步：创建本地订单...");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO `order` (user_id, product, amount, status) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, product);
            ps.setBigDecimal(3, amount);
            ps.setString(4, "CREATED");
            return ps;
        }, keyHolder);
        
        Long orderId = keyHolder.getKey().longValue();
        log.info("订单创建成功，orderId={}", orderId);
        
        log.info("========== 创建订单完成 ==========");
        return Map.of("code", 200, "message", "订单创建成功", "orderId", orderId);
    }
    
    /**
     * 测试回滚：故意抛异常
     */
    @GlobalTransactional(name = "create-order-rollback", rollbackFor = Exception.class)
    @GetMapping("/create-with-error")
    public Map<String, Object> createOrderWithError(
            @RequestParam Long userId,
            @RequestParam String product,
            @RequestParam BigDecimal amount) {
        
        log.info("========== 创建订单（测试回滚）开始 ==========");
        log.info("全局事务 XID: {}", RootContext.getXID());
        
        // 1. 远程调用扣款（会成功）
        log.info("第一步：调用 user-service 扣款...");
        Map<String, Object> deductResult = userClient.deductBalance(userId, amount);
        log.info("扣款结果: {}", deductResult);
        
        // 2. 故意抛异常，测试回滚
        log.info("第二步：故意抛异常，触发回滚...");
        throw new RuntimeException("模拟业务异常，触发分布式事务回滚！");
    }
}
```

### 5. 测试验证

**正常流程**：

```bash
# 1. 扣款+下单（正常）
curl "http://localhost:8082/order/create?userId=1&product=MacBook&amount=100"

# 查看结果：
# - user 表：balance 减少 100
# - order 表：新增一条订单记录
# - Seata Server 控制台：能看到全局事务记录
```

**回滚测试**：

```bash
# 2. 测试回滚
curl "http://localhost:8082/order/create-with-error?userId=1&product=MacBook&amount=100"

# 预期结果：
# - user 表：balance 不变（回滚了）
# - order 表：没有新订单
# - Seata Server 日志：显示 Rollback successfully
```

### 6. 查看 Seata 控制台

```
http://localhost:7091
```

可以看到：
- **全局事务列表**：显示所有全局事务（XID、状态、时间）
- **分支事务列表**：显示每个全局事务下的分支事务
- **锁列表**：显示当前被全局锁锁住的资源

### 7. 日志关键词

```bash
# 开启全局事务
[GlobalTransactionalExecutor] Begin new global transaction

# XID 传递
[RootContext] bind to context

# 分支事务注册
[RegisterTMServiceOnclientProcessor] register to TC success

# 回滚成功
Rollback global transaction successfully, xid = xxx
```

---

### XID 跨服务传递原理

`spring-cloud-starter-alibaba-seata` 自动实现了 XID 的传递：

```
order-service (TM)                    user-service (RM)
     │                                      │
     ├─ @GlobalTransactional               │
     ├─ 获取 XID: 192.168.x:8091:12345     │
     │                                      │
     ├─ Feign 调用 ─── HTTP Header ───────→│
     │   (自动添加 XID 到请求头)              │
     │   Header: TX_XID=192.168.x:8091:12345│
     │                                      ├─ 从 Header 提取 XID
     │                                      ├─ 绑定到当前线程
     │                                      ├─ 注册分支事务到 TC
     │                                      └─ 执行本地 SQL + undo_log
```

> 这个传递是通过 Seata 内置的 Feign 拦截器自动完成的，不需要你手动传递 XID。

---

## 常见问题排查

### 1. 客户端连不上 Seata Server

检查清单：
- [ ] Seata Server 是否启动成功（日志看 `Server started`）
- [ ] Nacos 服务列表是否能看到 `seata-server`
- [ ] 客户端 `registry.nacos.namespace` 是否与 Seata Server 一致
- [ ] 客户端 `registry.nacos.server-addr` 是否正确
- [ ] 防火墙是否开放了 8091 端口

### 2. 全局事务不生效

检查清单：
- [ ] `@GlobalTransactional` 注解是否加了（不是 `@Transactional`）
- [ ] 是否引入了 `spring-cloud-starter-alibaba-seata` 依赖
- [ ] `seata.enabled: true` 是否配置
- [ ] `tx-service-group` 是否与 Nacos 中的 `vgroupMapping` 对应
- [ ] 启动日志搜索 `GlobalTransactionScanner`，确认初始化成功

### 3. undo_log 表没有记录

可能原因：
- 业务方法没有抛异常，全局事务正常提交后 undo_log 会被清理
- 要观察 undo_log，可以在二阶段提交前打个断点或加 `Thread.sleep`
- 数据源代理未生效（检查 `seata.enable-auto-data-source-proxy` 是否为 false）

### 4. 回滚失败：Could not found global transaction

可能原因：
- Seata Server 的 `store.mode` 配置不对
- Seata 数据库的 `global_table` 没有建
- 全局事务已超时被自动清理

---

## 总结

```
┌────────────────────────────────────────────────────────────────┐
│                    Seata 核心知识                                 │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  问题：多服务 + 多数据库 → 本地事务管不了全局一致性                │
│                                                                 │
│  解决：Seata = 分布式事务的"乐团指挥"                             │
│                                                                 │
│  三个角色：                                                      │
│  ├─ TC（事务协调者）：Seata Server，统一指挥                      │
│  ├─ TM（事务管理器）：发起全局事务的服务                           │
│  └─ RM（资源管理器）：参与分支事务的服务                           │
│                                                                 │
│  四种模式：                                                      │
│  ├─ AT（重点）：无侵入，undo_log 自动回滚                         │
│  ├─ TCC：手动 Try/Confirm/Cancel                                │
│  ├─ Saga：长事务，正向+补偿                                      │
│  └─ XA：数据库级两阶段提交                                       │
│                                                                 │
│  AT 模式关键：                                                   │
│  ├─ DataSourceProxy 拦截 SQL，自动生成前后镜像                    │
│  ├─ 一阶段：业务 SQL + undo_log 在同一本地事务提交                │
│  ├─ 二阶段提交：异步清理 undo_log                                │
│  ├─ 二阶段回滚：beforeImage 反向补偿 + 脏写校验                  │
│  ├─ 全局锁：TC 端维护，防止其他事务脏写                           │
│  └─ @GlobalTransactional + undo_log 表 + 客户端配置              │
│                                                                 │
│  搭建步骤：                                                      │
│  ├─ Seata Server：下载 → 建库 → 改 yml → 配 Nacos → 启动       │
│  ├─ 客户端：加依赖 → 建 undo_log → 配 yml → 加注解              │
│  └─ 关键配置：tx-service-group 与 vgroupMapping 对应             │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

---

## 参考资料

- Seata 官方文档：https://seata.apache.org/zh-cn/docs/overview/what-is-seata
- Seata 新手部署指南：https://seata.apache.org/zh-cn/docs/v2.0/ops/deploy-guide-beginner/
- Spring Cloud Alibaba 版本说明：https://sca.aliyun.com/docs/2023/overview/version-explain/
- Seata AT 模式原理：https://seata.apache.org/zh-cn/docs/dev/mode/at-mode
- Seata Nacos 配置中心集成：https://seata.apache.org/zh-cn/docs/user/configuration/nacos/
