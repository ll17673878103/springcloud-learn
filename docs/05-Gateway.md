# 第七阶段：API 网关 - Spring Cloud Gateway

## 本阶段目标

- 理解 API 网关在微服务架构中的作用
- 掌握 Spring Cloud Gateway 的核心概念（Route / Predicate / Filter）
- 搭建 Gateway 并实现路由转发
- 掌握全局过滤器（GlobalFilter）统一鉴权
- 了解跨域配置、限流等高级功能

---

## 为什么需要 API 网关？

### 没有网关的问题

```
┌─────────┐    → user-service:8081/user/1
│  客户端  │    → order-service:8082/order/create
│         │    → product-service:8083/product/list
└─────────┘    → payment-service:8084/pay
               （客户端要记住所有服务的地址！）
```

**问题**：
- 客户端需要知道每个服务的地址和端口
- 每个服务都要各自做鉴权、日志、跨域处理
- 服务扩容/缩容后，客户端需要更新配置

### 有了网关之后

```
┌─────────┐         ┌──────────────┐
│  客户端  │ ──────→ │  Gateway     │ ──→ user-service:8081
│         │  统一    │  :8080      │ ──→ order-service:8082
└─────────┘  入口    │             │ ──→ product-service:8083
                   └──────────────┘
                   （统一鉴权、限流、日志、跨域）
```

**好处**：
- 客户端只需要知道 Gateway 的地址
- 鉴权、限流、日志统一在 Gateway 处理
- 后端服务变化对客户端透明

---

## Spring Cloud Gateway 核心概念

### 三个核心概念

| 概念 | 类比 | 说明 |
|------|------|------|
| **Route（路由）** | 快递分拣规则 | 定义请求转发的规则（ID + 目标URI + 断言 + 过滤器） |
| **Predicate（断言）** | 判断条件 | 匹配请求的规则（路径、方法、Header 等） |
| **Filter（过滤器）** | 安检门 | 请求经过时做什么处理（鉴权、加 Header、限流） |

### 请求处理流程

```
请求进来
  → Handler Mapping（路由匹配：用 Predicate 逐个匹配 Route）
    → Filter Chain（Pre Filter：鉴权、改路径、加 Header）
      → Proxy（代理转发：lb:// 从 Nacos 找实例）
        → 后端微服务处理
          → Filter Chain（Post Filter：记录日志、改响应）
            → 返回给客户端
```

### 重要限制

| ✅ 能用 | ❌ 不能用 |
|---------|----------|
| `spring-cloud-starter-gateway` | `spring-boot-starter-web`（会冲突！） |
| WebFlux 的 `ServerWebExchange` | `HttpServletRequest`（Servlet 体系的） |
| `GlobalFilter` | `HandlerInterceptor`（MVC 体系的） |

> **Gateway 底层是 WebFlux + Netty，不是 Servlet（Tomcat），绝对不能引入 `spring-boot-starter-web`！**

---

## Gateway 与现有架构的关系

```
客户端 ──→ Gateway ──→ order-service ──(Feign)──→ user-service
  │          │              │                      │
  │          │              └── 服务间用 Feign       │
  │          └── 外部入口用 Gateway                  │
  └── 不需要知道后端有多少服务
```

- **Gateway**：给"外部客户端"用的统一入口
- **Feign**："服务之间"的调用方式
- 两者互不干扰

---

## 项目结构

```
springcloud-learn/
├── gateway/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/learn/gateway/
│       │   └── GatewayApplication.java
│       └── resources/
│           └── application.yml
└── 其他服务...
```

---

## 依赖配置

### gateway/pom.xml

```xml
<dependencies>
    <!-- Gateway 核心（自带 WebFlux + Netty，不要加 spring-boot-starter-web！） -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>

    <!-- Nacos 服务发现（网关需要从 Nacos 拉取服务列表） -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- 负载均衡（配合 lb:// 使用） -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

---

## 配置文件

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: 你的namespace-id    # ⚠️ 必须和其他服务一致！
    gateway:
      routes:
        # 路由一：用户服务
        - id: user-service-route       # 路由 ID（唯一标识）
          uri: lb://user-service       # 目标地址（lb:// = 负载均衡 + 服务名）
          predicates:
            - Path=/api/user/**        # 匹配条件：路径以 /api/user 开头
          filters:
            - StripPrefix=1            # 转发前去掉第一层路径（/api）

        # 路由二：订单服务
        - id: order-service-route
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
          filters:
            - StripPrefix=1
```

### 配置详解

| 配置项 | 说明 |
|--------|------|
| `id` | 路由的唯一标识，随意命名 |
| `uri: lb://user-service` | `lb://` 表示通过负载均衡从 Nacos 查找服务实例 |
| `predicates: - Path=/api/user/**` | 只有路径匹配的请求才走这条路由 |
| `filters: - StripPrefix=1` | 转发前去掉路径的第一段（`/api/user/1` → `/user/1`） |

### lb:// 的含义

```
uri: lb://user-service
        │
        ▼
Gateway 看到 lb:// 前缀
        │
        ▼
问 Nacos："user-service 有哪些实例？"
        │
        ▼
Nacos 返回实例列表
        │
        ▼
LoadBalancer 根据策略选一个（默认轮询）
        │
        ▼
实际转发到：http://具体IP:端口/user/1
```

### 请求流转示例

```
客户端请求：GET http://localhost:8080/api/user/1

  → Gateway 收到请求
  → Predicate 匹配：Path=/api/user/** ✅
  → Filter StripPrefix=1：/api/user/1 → /user/1
  → LoadBalancer 从 Nacos 找到 user-service 实例
  → 转发到：http://具体IP:8081/user/1
  → 返回结果
```

---

## 常用的 Predicate（断言）

| 断言 | 说明 | 示例 |
|------|------|------|
| `Path` | 路径匹配 | `- Path=/api/user/**` |
| `Method` | HTTP 方法匹配 | `- Method=GET,POST` |
| `Header` | 请求头匹配 | `- Header=X-Request-Id, \d+` |
| `Query` | 查询参数匹配 | `- Query=name, admin` |
| `After` | 时间之后 | `- After=2024-01-01T00:00:00+08:00[Asia/Shanghai]` |
| `Between` | 时间范围内 | `- Between=开始时间, 结束时间` |

多个断言同时使用时，**必须全部匹配**才命中路由。

---

## 常用的 Filter（过滤器）

| 过滤器 | 说明 | 示例 |
|--------|------|------|
| `StripPrefix` | 去掉路径前 N 层 | `- StripPrefix=1` |
| `AddRequestHeader` | 添加请求头 | `- AddRequestHeader=X-Source, gateway` |
| `AddRequestParameter` | 添加查询参数 | `- AddRequestParameter=flag, true` |
| `RewritePath` | 路径重写 | `- RewritePath=/api/(?<segment>.*), /$\{segment}` |

---

## 测试验证

### 前提：namespace 必须一致

| 服务 | namespace |
|------|-----------|
| user-service | 必须相同 |
| order-service | 必须相同 |
| gateway | 必须相同 |

> 如果 namespace 不一致，Gateway 找不到其他服务，会报 503。

### 测试步骤

```powershell
# 1. 按顺序启动：Nacos → user-service → order-service → gateway

# 2. 直接访问 user-service（不走 Gateway）
curl http://localhost:8081/user/list

# 3. 通过 Gateway 访问（走 Gateway）
curl http://localhost:8080/api/user/list

# 4. 获取 Token
curl -X POST "http://localhost:8081/auth/login?username=admin&password=123456"

# 5. 通过 Gateway + Token 访问
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/user/1

# 6. 通过 Gateway 访问 order-service
curl -H "Authorization: Bearer <token>" "http://localhost:8080/api/order/create?userId=1"
```

---

## 常见踩坑

### 坑 1：Predicate 名字大小写

```yaml
# ❌ 错误
- path=/api/user/**

# ✅ 正确（P 大写）
- Path=/api/user/**
```

> 断言名称区分大小写！必须首字母大写。

### 坑 2：YAML 列表项格式

```yaml
# ❌ 错误：- 后没有空格
-StripPrefix=1

# ✅ 正确：- 后加空格
- StripPrefix=1
```

> YAML 语法中 `-` 是列表标记，后面必须跟一个空格。

### 坑 3：引入了 spring-boot-starter-web

Gateway 和 `spring-boot-starter-web` 不兼容，会导致启动失败。
Gateway 使用 WebFlux + Netty，不是 Servlet + Tomcat。

### 坑 4：namespace 不一致

Gateway、user-service、order-service 的 Nacos namespace 必须一致，
否则 Gateway 通过 `lb://服务名` 找不到服务实例。

---

## 下一步学习

- [ ] GlobalFilter 全局过滤器（统一鉴权）
- [ ] 自定义过滤器（请求日志、统一响应）
- [ ] 跨域配置（CORS）
- [ ] Gateway 整合 Sentinel（网关层限流）

---

## 下阶段预告

- **第八阶段：链路追踪** - 使用 Micrometer Tracing + Zipkin

---

## 本章小结

| 概念 | 说明 |
|------|------|
| **API 网关** | 微服务的统一入口，负责路由转发、鉴权、限流等 |
| **Route** | 路由规则，由 ID + URI + Predicate + Filter 组成 |
| **Predicate** | 断言条件，决定请求走哪条路由（Path、Method、Header 等） |
| **Filter** | 过滤器，请求前/后做处理（StripPrefix、AddHeader 等） |
| **lb://** | 负载均衡协议前缀，从注册中心查找服务实例 |
| **StripPrefix** | 去掉路径前 N 层，让 Gateway 路径和后端路径对齐 |
| **WebFlux** | Gateway 底层使用响应式编程，不能引入 spring-boot-starter-web |
