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

---

# 第三阶段（续）：配置中心 - Nacos

## 本部分目标

- 理解 Nacos 配置中心的作用
- 掌握多环境配置隔离（namespace + profiles）
- 掌握配置热更新
- 掌握配置监听

---

## Nacos 配置中心核心概念

```
┌─────────────────────────────────────────────────────────────┐
│                    Nacos 配置中心                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   命名空间 (Namespace)                                       │
│   ├── dev 环境                                             │
│   ├── test 环境                                            │
│   └── pro 环境                                             │
│                                                             │
│   分组 (Group)                                              │
│   └── DEFAULT_GROUP / 自定义分组                             │
│                                                             │
│   配置 (Data ID)                                           │
│   └── user-service-dev.yaml                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 三个核心概念

| 概念 | 作用 | 类比
|------|------|------
| **Namespace** | 环境隔离 | 不同的文件夹（dev/pro/test）
| **Group** | 分组管理 | 文件夹内的子分类
| **Data ID** | 具体配置文件 | 文件名

---

## bootstrap.yml 配置

### 基础配置

`user-service/src/main/resources/bootstrap.yml`：

```yaml
spring:
  application:
    name: user-service                    # 应用名（Data ID 的一部分）

  # Nacos 配置中心
  config:
    import: optional:nacos:${spring.application.name}-${spring.profiles.active}
    # 格式: optional:nacos:应用名-环境.后缀
    # 示例: optional:nacos:user-service-pro.yaml

  # 激活的环境
  profiles:
    active: pro                          # 决定加载哪个配置文件

  cloud:
    nacos:
      server-addr: 101.43.103.32:8848   # Nacos 地址

      # 服务发现
      discovery:
        namespace: c8086219-54ec-4b09-bcc3-1b13193f770e    # 命名空间 ID

      # 配置中心
      config:
        namespace: c8086219-54ec-4b09-bcc3-1b13193f770e  # 命名空间 ID
        file-extension: yaml                                  # 配置文件格式
        group: DEFAULT_GROUP                                 # 分组
```

### Data ID 规则

```
${spring.application.name}-${spring.profiles.active}.${file-extension}

示例：
  user-service + pro + yaml = user-service-pro.yaml
  user-service + dev + yaml = user-service-dev.yaml
```

---

## 多环境配置隔离

### 命名空间（Namespace）

用于隔离不同环境（开发、测试、生产）：

| 环境 | Namespace ID | 说明 |
|------|-------------|------|
| dev | c8086219-... | 开发环境 |
| test | xxx | 测试环境 |
| pro | yyy | 生产环境 |

**注意**：Namespace 使用 **ID** 而非名称

### 本地与线上配置切换

```
profiles.active = local  →  Nacos 中没有 user-service-local.yaml → 使用本地 application.yml
profiles.active = dev    →  加载 Nacos 的 user-service-dev.yaml
profiles.active = pro    →  加载 Nacos 的 user-service-pro.yaml
```

#### 切换方式

**方式一**：修改 bootstrap.yml
```yaml
profiles:
  active: local   # 本地开发
  # active: pro    # 线上
```

**方式二**：启动参数
```bash
java -jar app.jar --spring.profiles.active=pro
```

---

## 配置热更新

### 方式一：@RefreshScope + @Value（最常用）

```java
@Slf4j
@RestController
@RequestMapping("/api/config")
@RefreshScope                           // 配置变化后自动刷新 Bean
public class ConfigDemoController {

    @Value("${user.cache.timeout:300}")
    private int cacheTimeout;

    @GetMapping("/current")
    public String getConfig() {
        return "缓存超时: " + cacheTimeout;
    }
}
```

**特点**：
- 配置变化后，下次访问接口自动获取新值
- 被 `@RefreshScope` 修饰的 Bean 会重新创建

### 方式二：@ConfigurationProperties

```java
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "user.cache")
public class CacheConfig {
    private int timeout = 300;
}
```

### 方式三：@NacosConfigurationProperties（阿里特有）

```java
@Data
@Component
@NacosConfigurationProperties(
    dataId = "user-service-pro.yaml",
    group = "DEFAULT_GROUP",
    autoRefreshed = true
)
public class NacosCacheConfig {
    private int timeout;
}
```

---

## 配置监听

### 方式一：实现 Listener 接口（Nacos 层面）

监听 Nacos 配置变化（主动感知）：

```java
@Slf4j
@Component
public class NacosConfigListener implements Listener {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("========== Nacos 配置变化 ==========");
        log.info("配置内容: {}", configInfo);

        // 清除缓存
        redisTemplate.delete("user:cache:list");

        log.info("========== 缓存已清除 ==========");
    }
}
```

注册监听器：

```java
@Component
public class NacosConfigService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    private NacosConfigListener nacosConfigListener;

    @PostConstruct
    public void init() {
        // 动态构建 Data ID
        String dataId = applicationName + "-" + activeProfile + ".yaml";

        ConfigService configService = NacosFactory.createConfigService(properties);
        configService.addListener(dataId, "DEFAULT_GROUP", nacosConfigListener);
    }
}
```

### 方式二：@EventListener（Spring 层面）

监听 Spring 事件（RefreshScope 刷新时触发）：

```java
@Slf4j
@Component
public class ConfigRefreshEventListener {

    @EventListener
    public void onRefreshScope(RefreshScopeRefreshedEvent event) {
        log.info("========== RefreshScope 刷新事件 ==========");
        log.info("来源: {}", event.getSource());

        // 通知后台管理系统
        notifyAdmin();
    }

    private void notifyAdmin() {
        // 发送 WebSocket 通知
        // 发送邮件/短信
        // 记录日志
    }
}
```

### 两种监听方式对比

| 特点 | `implements Listener` | `@EventListener` |
|------|----------------------|------------------|
| 监听源 | Nacos Server | Spring 框架 |
| 触发时机 | Nacos 配置发布时 | @RefreshScope 刷新时 |
| 能获取 | 完整配置内容 | 变化的环境变量名 |

```
Nacos 配置在网页上被修改
        ↓
   实现 Listener 接口的类收到通知
        ↓
   你想做什么就做什么（清缓存、发通知等）

---

@RefreshScope 刷新了
        ↓
   @EventListener 监听到事件
        ↓
   你想做什么就做什么（清缓存、发通知等）
```

---

## 完整集成示例

### 架构图

```
┌──────────────────────────────────────────────────────────────┐
│                        Nacos Server                          │
│                   (命名空间: c8086219-...)                  │
│                   (Data ID: user-service-pro.yaml)           │
└────────────────────────────┬─────────────────────────────────┘
                             │ 长轮询检测配置变化
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                     user-service 应用                         │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ NacosConfigListener (implements Listener)               │ │
│  │ receiveConfigInfo() → 清除 Redis 缓存                   │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ @RefreshScope + @Value                                  │ │
│  │ 配置变化后，下次访问接口自动获取新值                      │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ ConfigRefreshEventListener (@EventListener)              │ │
│  │ onRefreshScope() → 通知后台管理系统                      │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                        Redis                                  │
│              (user:cache:* 缓存自动清除)                      │
└──────────────────────────────────────────────────────────────┘
```

### 流程

1. **启动应用** → NacosConfigService 初始化 → 注册监听器
2. **修改 Nacos 配置** → Nacos 推送变化 → NacosConfigListener 收到通知
3. **清除 Redis 缓存** → 下次查询从数据库重新加载
4. **@EventListener** 通知后台管理系统配置已更新

---

## 常见问题

### Q1: namespace 用 ID 还是名称？

**使用 ID**。名称可以随意命名，但 ID 是固定的。

### Q2: profiles.active 和 namespace 有什么关系？

**没有关系**。
- `profiles.active` → 决定 Data ID（文件名）
- `namespace` → 决定在哪个命名空间查找

### Q3: 本地开发和线上如何切换？

- `profiles.active = local` → Nacos 中没有对应配置 → 使用本地 application.yml
- `profiles.active = dev/pro` → 加载 Nacos 配置

### Q4: 配置变化后为什么没触发监听？

检查：
1. Nacos 中是否有对应的 Data ID 配置文件？
2. 配置内容是否为空？
3. namespace ID 是否正确？

---

## 下阶段预告

- **第四阶段：服务调用** - OpenFeign 远程调用
