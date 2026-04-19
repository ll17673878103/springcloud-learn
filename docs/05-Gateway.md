# 第七阶段：API 网关 - Spring Cloud Gateway

## 本阶段目标

- 理解 API 网关在微服务架构中的作用
- 掌握 Spring Cloud Gateway 的核心概念（Route / Predicate / Filter）
- 搭建 Gateway 并实现路由转发
- 掌握全局过滤器（GlobalFilter）统一鉴权
- 掌握自定义过滤器（请求日志、Pre/Post）
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

### 坑 5：路由未覆盖所有路径

如果 AuthController 的路径是 `/auth/login`，而 Gateway 路由只配了 `/api/user/**`，
那访问 `/api/auth/login` 会返回 404。需要在 Predicate 里把所有需要转发的路径都配上去：

```yaml
predicates:
  - Path=/api/user/**,/api/auth/**,/api/config/**  # 多个路径用逗号分隔
```

### 坑 6：Gateway 不能用 Servlet 的东西

写 GlobalFilter 时，不能用 `HttpServletRequest`、`HttpServletResponse`、`HandlerInterceptor`，
这些是 Servlet（Tomcat）体系的。Gateway 底层是 WebFlux + Netty，必须用：

| Servlet（不能用） | WebFlux（必须用） |
|---|---|
| `HttpServletRequest` | `ServerWebExchange.getRequest()` |
| `HttpServletResponse` | `ServerWebExchange.getResponse()` |
| `request.setAttribute()` | `exchange.getAttributes().put()` |
| `request.getAttribute()` | `exchange.getAttribute()` |
| `response.getWriter()` | `DataBuffer` + `response.writeWith()` |

---

## GlobalFilter 统一鉴权

### 为什么要在 Gateway 做鉴权

| 方案 | 问题 |
|------|------|
| 每个微服务各自鉴权 | 容易遗漏，直接访问端口可绕过鉴权 |
| Gateway 统一鉴权 ✅ | 所有请求先经过“安检门”，一处搞定 |

### 核心文件

```
gateway/src/main/java/com/learn/gateway/
├── filter/AuthGlobalFilter.java   # 统一鉴权过滤器
└── util/JwtUtil.java             # Gateway 专用 JWT 解析工具
```

### AuthGlobalFilter 工作流程

```
请求进来
  │
  ├─ 白名单路径？（/auth/login、/auth/**）
  │    └─ 是 → 直接放行 ✅
  │
  ├─ 有 Authorization 请求头？
  │    └─ 没有 → 返回 401 ❌
  │
  ├─ Token 有效？
  │    └─ 过期/无效 → 返回 401 ❌
  │
  └─ 有效 → 把 userId/username 放进请求头 → 放行 ✅
```

### 关键代码

```java
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    // 白名单
    private static final List<String> WHITE_LIST = List.of(
            "/auth/login",
            "/auth/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String realPath = path.startsWith("/api/") ? path.substring(4) : path;

        // 白名单放行
        if (isWhiteListed(realPath)) {
            return chain.filter(exchange);
        }

        // 检查 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), "未登录，请先获取 Token");
        }

        try {
            Claims claims = JwtUtil.parseToken(authHeader.substring(7));
            // 将用户信息传递给下游服务
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
                    .header("X-Username", claims.getSubject())
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "Token 无效");
        }
    }

    @Override
    public int getOrder() { return -1; }  // 最高优先级
}
```

### 关键知识点

1. **`implements GlobalFilter`** — 对所有路由生效
2. **`implements Ordered`** — `getOrder()` 返回数字越小，优先级越高
3. **`chain.filter(exchange)`** — 放行请求到下一个过滤器/后端服务
4. **不调用 `chain.filter()`** — 拦截请求，直接返回响应
5. **`request.mutate().header(...)`** — 向下游服务传递用户信息
6. **白名单机制** — 登录接口不需要 Token，直接放行

---

## 自定义过滤器 - 请求日志

### Pre Filter 和 Post Filter

```
chain.filter() 之前的代码    → Pre 阶段（请求前处理）
chain.filter()               → 请求转发到后端
chain.filter().then(...)     → Post 阶段（响应回来后处理）
```

### 核心文件

```
gateway/src/main/java/com/learn/gateway/filter/RequestLogFilter.java
```

### 效果

```
>>> Gateway 收到请求: GET /api/user/list
<<< Gateway 响应完成: GET /api/user/list | 状态码: 200 | 耗时: 45ms
```

### 关键知识点

1. **`chain.filter(exchange).then(Mono.fromRunnable(...))`** — Post 阶段写法
2. **`exchange.getAttributes()`** — Pre 和 Post 阶段共享数据的桥梁（Map 结构）
3. **`order = 0`** — 在鉴权过滤器（-1）之后执行

---

## 跨域配置（CORS）

### 什么是跨域

浏览器同源策略：前端 `http://localhost:3000` 访问 Gateway `http://localhost:8080`，端口不同会被浏览器拦截。
CORS 配置就是告诉浏览器"我允许跨域"。

### 配置方式（application.yml）

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins:
              - "http://localhost:3000"
              - "http://localhost:5173"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS              # 预检请求必须允许
            allowed-headers:
              - "*"
            allow-credentials: true
            max-age: 3600
```

### 配置项速查

| 配置项 | 意义 |
|--------|------|
| `allowed-origins` | 允许哪些前端地址跨域访问 |
| `allowed-methods` | 允许哪些 HTTP 方法（OPTIONS 必须加） |
| `allowed-headers` | 允许哪些请求头（`*` 含 Authorization） |
| `allow-credentials: true` | 允许携带 Cookie / Token |
| `max-age: 3600` | 预检结果缓存 1 小时 |

> **踩坑**：CORS 只在 Gateway 配一次就行，后端微服务不要重复配，否则会冲突。

---

## Sentinel 网关限流

### 两层 Sentinel 防护

```
客户端请求
  → Gateway（Sentinel 限流）         ← 第一道门：QPS 超限返回 429
    → order-service（Sentinel 熔断）  ← 第二道门：调用异常时熔断降级
      → user-service
```

### 核心文件

```
gateway/src/main/java/com/learn/gateway/config/SentinelGatewayConfig.java
```

### 关键依赖

```xml
<!-- Gateway 专用 Sentinel 适配（WebFlux 环境） -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

> 注意：Gateway 不能用普通的 `spring-cloud-starter-alibaba-sentinel`，需要额外加 adapter。

### 限流规则

```java
// 按 route ID 限流，route ID 对应 application.yml 中的路由 ID
Set<GatewayFlowRule> rules = new HashSet<>();
rules.add(new GatewayFlowRule("user-service-routes")
        .setCount(5)        // QPS 阈值：每秒 5 次
        .setIntervalSec(1)  // 统计时间窗口：1 秒
);
GatewayRuleManager.loadRules(rules);
```

### 自定义限流响应

```java
GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
        return ServerResponse
                .status(HttpStatus.TOO_MANY_REQUESTS)  // 429
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        "{\"code\":429,\"message\":\"请求太频繁，请稍后再试\"}"
                ));
    }
});
```

### 测试限流

```powershell
# 1. 登录获取 Token
$loginResp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST -Body @{username="admin";password="123456"} `
    -ContentType "application/x-www-form-urlencoded"
$token = $loginResp.data.token

# 2. 快速发 10 个请求触发限流
1..10 | ForEach-Object {
    try {
        $resp = Invoke-WebRequest `
            -Uri "http://localhost:8080/api/order/create?userId=1" `
            -Headers @{Authorization="Bearer $token"} -UseBasicParsing
        Write-Host "Request $_ : $($resp.StatusCode) - OK"
    } catch {
        Write-Host "Request $_ : 429 - BLOCKED!" -ForegroundColor Red
    }
}
```

结果：前 3 个通过（阈值 3/秒），第 4 个开始返回 429。

---

## Gateway 过滤器执行顺序

```
请求进来
  │
  ├─ SentinelGatewayFilter（order = HIGHEST_PRECEDENCE）← 限流检查
  ├─ AuthGlobalFilter（order = -1）                     ← 鉴权检查
  ├─ RequestLogFilter（order = 0）                      ← 记录开始时间
  │
  │  chain.filter(exchange) → 转发到后端服务
  │
  ├─ RequestLogFilter Post 阶段                        ← 记录耗时和状态码
  │
  └─ 返回客户端
```

### order 值的习惯

| 值 | 用途 | 说明 |
|---|---|---|
| 最高 | Sentinel 限流 | 最外层防护 |
| -1 | 鉴权 | 身份验证 |
| 0 | 日志 | 记录放行后的请求 |

> `getOrder()` 是 Spring 生态的通用排序机制，数字越小优先级越高。

---

## 下一步学习

- [x] GlobalFilter 全局过滤器（统一鉴权）
- [x] 自定义过滤器（请求日志、统一响应）
- [x] 跨域配置（CORS）
- [x] Gateway 整合 Sentinel（网关层限流）

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
| **GlobalFilter** | 对所有路由生效的过滤器，用于鉴权、日志等 |
| **ServerWebExchange** | WebFlux 的请求上下文（替代 Servlet 的 HttpServletRequest） |
| **GatewayFilterChain** | 过滤器链条，chain.filter() 放行，不调用则拦截 |
| **getOrder()** | 过滤器优先级，数字越小越先执行 |
| **CORS** | 跨域配置，只在 Gateway 配一次，后端不要重复配 |
| **sentinel-spring-cloud-gateway-adapter** | Gateway 专用 Sentinel 适配包（WebFlux 环境） |
| **GatewayFlowRule** | 路由级别的限流规则，按 route ID 限流 |
