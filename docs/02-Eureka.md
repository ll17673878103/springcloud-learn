# 第二阶段：服务注册与发现 - Eureka

## 本阶段目标

- 搭建 Eureka Server（服务注册中心）
- 创建 user-service 并注册到 Eureka
- 理解服务注册与发现的工作原理
- 搭建 Eureka 集群（高可用）

## 什么是服务注册与发现？

在微服务架构中，服务数量众多，每个服务的 IP 和端口可能随时变化。

**解决问题**：
- 服务 A 如何知道服务 B 在哪里？（传统方式：硬编码 IP）
- 服务 B 扩容/缩容后，服务 A 如何感知？（手动修改配置）

**解决方案**：引入一个"中介" - 注册中心

```
┌─────────────────────────────────────────────────────┐
│              服务注册与发现原理                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│   [服务A] ──────注册──────► [Eureka Server]         │
│   [服务B] ──────注册──────► [Eureka Server]         │
│   [服务C] ──────拉取──────► [Eureka Server]         │
│        ◄───获取服务列表───                          │
│                                                     │
│   服务B下线 ──通知──► [Eureka Server]              │
│        ◄───更新列表───                             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 什么是 Eureka？

Eureka 是 Netflix 开源的服务注册与发现组件，包含两部分：
- **Eureka Server**：服务注册中心
- **Eureka Client**：服务提供者/消费者

## Eureka 工作原理

1. **服务注册**：Eureka Client 启动时向 Eureka Server 注册自己的信息（IP、端口、服务名）
2. **服务续约**：Client 定期发送心跳（默认30秒），证明自己还活着
3. **服务下线**：Client 关闭时通知 Server 删除服务实例
4. **服务拉取**：Client 定期从 Server 获取服务列表到本地缓存

## 项目结构

```
springcloud-learn/
├── pom.xml                    # 父项目
├── eureka-server/             # Eureka 注册中心
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../EurekaServerApplication.java
│       └── resources/application.yml
├── user-service/              # 用户服务
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../
│       │   ├── UserServiceApplication.java
│       │   └── controller/UserController.java
│       └── resources/application.yml
└── docs/
```

---

## 依赖配置

### 父 pom.xml 版本管理

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
</properties>
```

> 注意：Spring Cloud 2023.0.0 重新引入了 Eureka 4.1.0，支持 Spring Boot 3.x

### Eureka Server (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```

### Eureka Client (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

---

## 配置文件

### Eureka Server 单机版 (application.yml)

```yaml
server:
  port: 7001

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    # 不注册到自身（单节点）
    register-with-eureka: false
    # 不从自身拉取服务列表
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:7001/eureka/
  server:
    # 关闭自我保护模式（开发环境）
    enable-self-preservation: false
```

### Eureka Client - user-service (application.yml)

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
  instance:
    # 显示 IP 而不是主机名
    prefer-ip-address: true
```

---

## 启动类注解

### Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### Eureka Client

```java
@SpringBootApplication
// @EnableDiscoveryClient 是通用注解，支持多种注册中心
// @EnableEurekaClient 只支持 Eureka
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

---

## 启动测试步骤

### 步骤 1：启动 Eureka Server

```powershell
cd eureka-server
mvn spring-boot:run
```

访问控制台：`http://localhost:7001`

### 步骤 2：启动 user-service

新开终端：

```powershell
cd user-service
mvn spring-boot:run
```

### 步骤 3：验证

1. 打开 `http://localhost:7001`
2. 在 "Instances currently registered with Eureka" 部分看到 `user-service`
3. 访问 `http://localhost:8081/user/1`，返回 `User 1`

---

## 核心概念

| 概念 | 说明 |
|------|------|
| `register` | 服务注册 - Client 告诉 Server 自己存在 |
| `renew` | 服务续约 - Client 定期发送心跳（默认30秒） |
| `cancel` | 服务下线 - Client 关闭时通知 Server |
| `fetch registries` | 获取服务列表 - Client 从 Server 拉取服务列表到本地 |
| `自我保护模式` | 网络分区时防止误删服务（生产环境开启） |

---

## 为什么选择 Eureka？

✅ 优点：
- 与 Spring Cloud 集成好
- 支持集群
- 有控制台界面
- 2023.0.0 版本重新支持

❌ 缺点：
- Netflix 已停止官方维护（Spring Cloud 接手）
- 不支持配置中心

---

## 拓展：Eureka 集群

### 什么是集群？

**单机版问题**：如果唯一的 Eureka Server 宕机，整个系统将无法工作。

**集群方案**：搭建多个 Eureka Server，它们之间相互同步数据，一台宕机不影响服务注册与发现。

### 集群架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Eureka 集群原理                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   [user-service] ──┐                                        │
│                    │                                        │
│        ┌──────────┴──────────┐                              │
│        ▼                     ▼                               │
│   ┌──────────┐         ┌──────────┐                        │
│   │ Eureka   │◄───────►│ Eureka   │                        │
│   │ Server 1 │  相互   │ Server 2 │                        │
│   │  :7001  │  注册   │  :7002   │                        │
│   └──────────┘         └──────────┘                        │
│        │                     │                               │
│        └──────────┬──────────┘                               │
│                   ▼                                         │
│            [服务列表同步]                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 核心原理

1. **相互注册**：Eureka Server 之间相互注册
2. **服务同步**：服务提供者注册到任意一台 Server，数据会自动同步到其他节点
3. **客户端配置**：客户端配置多个 Server 地址，用逗号分隔

### 搭建步骤

#### 步骤 1：创建 eureka-server-2 模块

复制 eureka-server 创建 eureka-server-2：

```
eureka-server-2/
├── pom.xml
└── src/main/
    ├── java/.../EurekaServerApplication.java
    └── resources/application.yml
```

#### 步骤 2：修改 eureka-server 配置

**eureka-server/src/main/resources/application.yml**

```yaml
server:
  port: 7001

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: true
    fetch-registry: true
    # 注册到 7002
    service-url:
      defaultZone: http://localhost:7002/eureka/
  server:
    enable-self-preservation: false
```

#### 步骤 3：修改 eureka-server-2 配置

**eureka-server-2/src/main/resources/application.yml**

```yaml
server:
  port: 7002

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: true
    fetch-registry: true
    # 注册到 7001
    service-url:
      defaultZone: http://localhost:7001/eureka/
  server:
    enable-self-preservation: false
```

#### 步骤 4：修改 user-service 配置

同时注册到两个 Eureka Server：

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/,http://localhost:7002/eureka/
  instance:
    prefer-ip-address: true
```

### 启动测试

1. 启动 eureka-server（端口7001）
2. 启动 eureka-server-2（端口7002）
3. 访问 `http://localhost:7001`，看到 "registered with peer" 
4. 访问 `http://localhost:7002`，看到 "registered with peer"
5. 启动 user-service
6. 两个控制台都应该看到 user-service

### 父 pom.xml 添加模块

```xml
<modules>
    <module>eureka-server</module>
    <module>eureka-server-2</module>
    <module>user-service</module>
</modules>
```

---

## 控制台地址

| 服务 | 地址 |
|------|------|
| Eureka Server 1 | http://localhost:7001 |
| Eureka Server 2 | http://localhost:7002 |

---

## 下阶段预告

- **第三阶段：服务调用与负载均衡** - 使用 OpenFeign 实现服务间调用
