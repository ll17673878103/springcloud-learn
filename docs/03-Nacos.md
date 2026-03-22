# 第三阶段：服务注册与发现 - Nacos

## 本阶段目标

- 搭建 Nacos Server（服务注册中心 + 配置中心）
- 将 user-service 注册到 Nacos
- 理解 Nacos 与 Eureka 的区别

## 什么是 Nacos？

Nacos 是 Alibaba 开源的 **服务注册与发现** + **配置管理** 组件。

```
┌─────────────────────────────────────────────────────────────┐
│                      Nacos 功能                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────────┐    ┌─────────────────┐              │
│   │  服务注册与发现  │    │    配置管理     │              │
│   │                 │    │                 │              │
│   │  • 服务注册     │    │  • 配置管理     │              │
│   │  • 服务发现     │    │  • 配置变更     │              │
│   │  • 健康检查     │    │  • 配置监听     │              │
│   └─────────────────┘    └─────────────────┘              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 为什么选择 Nacos？

| 特性 | Nacos | Eureka |
|------|-------|--------|
| 服务注册与发现 | ✅ | ✅ |
| 配置中心 | ✅ | ❌ |
| 集群部署 | 简单 | 复杂 |
| 活跃度 | Alibaba 维护 | Spring Cloud 维护 |
| 国内流行度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

## Nacos vs Eureka

```
┌─────────────────────────────────────────────────────────────┐
│                    Nacos 与 Eureka 对比                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Eureka:                                                   │
│   [服务] ──注册──► [Eureka Server] ──同步──► [服务列表]  │
│                                                             │
│   Nacos:                                                    │
│   [服务] ──注册──► [Nacos Server]                          │
│              │                                              │
│              ├───  注册发现 ──► 实时推送                    │
│              └───  配置管理 ──► 热更新                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Nacos 架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Nacos 架构                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────────┐      ┌──────────────┐                 │
│   │  Nacos Server │◄────►│  Nacos Server │ (集群)        │
│   │    :8848     │      │    :8848     │                 │
│   └───────┬───────┘      └───────┬───────┘                 │
│           │                      │                          │
│           ▼                      ▼                          │
│   ┌──────────────┐      ┌──────────────┐                 │
│   │ user-service │      │ order-service │                 │
│   └──────────────┘      └──────────────┘                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 项目结构

```
springcloud-learn/
├── pom.xml
├── eureka-server/             # Eureka（保留）
├── user-service/             # 用户服务（已有）
├── order-service/            # 订单服务（后续创建）
└── docs/
```

---

## Nacos 安装与启动

### 方式一：Docker（推荐）

```powershell
# 启动 Nacos（单机模式）
docker run -d --name nacos -p 8848:8848 -p 9848:9848 nacos/nacos-server:v2.2.3

# 访问控制台
# http://localhost:8848/nacos
# 用户名: nacos
# 密码: nacos
```

### 方式二：本地安装

1. 下载：https://github.com/alibaba/nacos/releases
2. 解压
3. 启动（Windows）：`startup.cmd -m standalone`

---

## 将 user-service 接入 Nacos

### 步骤 1：添加 Nacos 依赖

修改 `user-service/pom.xml`，添加：

```xml
<dependencies>
    <!-- 保留 Eureka Client（可选） -->
    <!-- <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency> -->

    <!-- 添加 Nacos Discovery -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
</dependencies>
```

### 步骤 2：修改配置文件

修改 `user-service/src/main/resources/application.yml`：

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: public
```

### 步骤 3：修改启动类

确保有 `@EnableDiscoveryClient` 注解（已经有了）。

### 步骤 4：启动测试

1. 确保 Nacos 已启动
2. 启动 user-service
3. 访问 Nacos 控制台：`http://localhost:8848/nacos`
4. 在「服务管理」→「服务列表」中看到 `user-service`

---

## 同时注册到 Nacos 和 Eureka

如果想让服务同时注册到多个注册中心：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:7001/eureka/
```

---

## Nacos 控制台

| 功能 | 路径 |
|------|------|
| 服务列表 | 服务管理 → 服务列表 |
| 配置管理 | 配置管理 → 配置列表 |
| 新建服务 | 服务管理 → 服务注册 |

---

## 下阶段预告

- **第四阶段：服务调用与负载均衡** - 使用 OpenFeign + LoadBalancer
