# 07-Sentinel 服务熔断与降级

## 本阶段目标

- 理解为什么微服务需要熔断和降级
- 掌握 Sentinel 的核心概念（流控、熔断、降级）
- 理解 Sentinel 在网关层和服务间调用的不同用法
- 掌握 OpenFeign 集成 Sentinel 的方法（FallbackFactory）
- 理解二级防护架构的设计思路

---

## 为什么需要熔断和降级？

### 雪崩效应

```
用户请求 → A → B → C
                  ↓
            C 突然挂了
                  ↓
       B 等不到 C 响应，线程阻塞
                  ↓
       A 等不到 B 响应，线程阻塞
                  ↓
        所有线程耗尽，服务崩溃
                 
这就是经典的"雪崩效应"
```

**问题**：
- 一个服务慢，导致所有服务都慢
- 线程池资源耗尽
- 级联故障，最终整个系统宕机

### 解决方案：熔断 + 降级

| 机制 | 作用 | 比喻 |
|------|------|------|
| **熔断** | 检测到下游服务异常，自动"跳闸" | 保险丝烧断，停止供电 |
| **降级** | 熔断后返回一个合理的默认响应 | 停电时启动应急灯 |

---

## Sentinel 核心概念

### 三大功能

```
┌─────────────────────────────────────────────────────┐
│                    Sentinel                          │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────┐ │
│  │   流控(Flow) │    │  熔断(Degrade)│   │  降级   │ │
│  └─────────────┘    └─────────────┘    └─────────┘ │
│         │                  │                │       │
│    限制 QPS/并发      检测异常/慢调用    提供兜底    │
│                                                     │
└─────────────────────────────────────────────────────┘
```

| 功能 | 说明 | 触发条件 |
|------|------|----------|
| **流控** | 限制流量进入速度 | QPS/并发超过阈值 |
| **熔断** | 当下游异常比例/慢调用比例达到阈值，自动熔断 | 错误率/慢调用比例超限 |
| **降级** | 熔断后调用 fallback 方法返回兜底数据 | 熔断期间所有请求 |

### Sentinel 应用场景

| 场景 | 位置 | 保护对象 | 本项目示例 |
|------|------|----------|------------|
| **网关限流** | Gateway | 外部请求入口 | 限制客户端 QPS |
| **服务间保护** | order-service | Feign 调用链 | 保护 user-service 调用 |

---

## 架构图：二级防护设计

```
┌─────────────────────────────────────────────────────────────────┐
│                      请求来源                                   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    第一级防护：Gateway 网关限流                    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  SentinelGatewayFilter + GatewayFlowRule                │    │
│  │  - 限制：10 QPS/路由                                     │    │
│  │  - 超出：返回 429 + 自定义 JSON                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                         放行 10 QPS                              │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   第二级防护：order-service 服务间保护            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  SentinelFeignIntegration                                 │    │
│  │  - 流控：限制 5 QPS 对 user-service 的调用                │    │
│  │  - 熔断：50% 慢调用(>500ms) 或 50% 异常 → 熔断 10s        │    │
│  │  - 降级：UserClientFallbackFactory 返回兜底数据           │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

**设计思路**：
- **第一级（网关）**：挡住外部大流量，阈值设置较高
- **第二级（服务内）**：精细化流量控制，阈值设置较低
- 两级配合，形成纵深防御

---

## 项目结构

```
springcloud-learn/
├── order-service/
│   ├── src/main/java/com/learn/orderservice/
│   │   ├── config/
│   │   │   └── SentinelRuleConfig.java    ← 流控+熔断规则配置
│   │   └── feign/
│   │       ├── UserClient.java            ← Feign 接口
│   │       └── UserClientFallbackFactory.java  ← 降级工厂
│   └── src/main/resources/
│       └── application.yml               ← Sentinel 配置
│
└── gateway/
    ├── src/main/java/com/learn/gateway/
    │   └── config/
    │       └── SentinelGatewayConfig.java ← 网关限流配置
    └── src/main/resources/
        └── application.yml
```

---

## 依赖配置

### order-service/pom.xml（服务间熔断降级）

```xml
<!-- Sentinel 核心（服务间调用保护） -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>

<!-- OpenFeign 开启 Sentinel 支持 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### gateway/pom.xml（网关限流）

```xml
<!-- Sentinel 网关适配（Gateway 用的是 WebFlux，需要专门的 adapter） -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
</dependency>

<!-- Sentinel 通用依赖 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

> ⚠️ **重要**：Gateway 使用的是 WebFlux（非 Servlet），所以需要 `sentinel-spring-cloud-gateway-adapter` 而不是普通的 Sentinel 依赖。

---

## 配置详解

### order-service 配置

**application.yml**：

```yaml
# Sentinel 配置
sentinel:
  transport:
    dashboard: localhost:8080    # Sentinel Dashboard 地址（可选，用于查看监控）
    port: 8719                    # 与 Dashboard 通信的端口
  eagerness: true                # 启动时立即初始化 Sentinel

# OpenFeign 开启 Sentinel 支持
feign:
  sentinel:
    enabled: true               # ✅ 关键配置！开启 Feign 对 Sentinel 的集成
```

### gateway 配置

**application.yml**：

网关限流规则通过代码配置（`SentinelGatewayConfig`），不需要在 yml 中额外配置。

---

## 第一级防护：Gateway 网关限流

### SentinelGatewayConfig.java

```java
@Configuration
public class SentinelGatewayConfig {

    /**
     * 配置 Sentinel 网关过滤器
     * 拦截所有请求，检查是否触发限流规则
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 配置异常处理器
     * 被限流时返回自定义 JSON
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 限制 user-service 路由：每秒最多 10 个请求
        rules.add(new GatewayFlowRule("user-service-routes")
                .setCount(10)       // QPS 阈值
                .setIntervalSec(1)  // 统计时间窗口：1秒
        );

        // 限制 order-service 路由：每秒最多 10 个请求
        rules.add(new GatewayFlowRule("order-service-route")
                .setCount(10)
                .setIntervalSec(1)
        );

        GatewayRuleManager.loadRules(rules);

        // 配置被限流时的响应
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {
            return ServerResponse
                    .status(HttpStatus.TOO_MANY_REQUESTS)  // 429
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            "{\"code\":429,\"message\":\"请求太频繁，请稍后再试\"}"
                    ));
        });
    }
}
```

### 配置说明

| 配置项 | 说明 | 本项目值 |
|--------|------|----------|
| `GatewayFlowRule.setCount()` | QPS 阈值 | 10 |
| `GatewayFlowRule.setIntervalSec()` | 统计时间窗口 | 1秒 |
| `GatewayFlowRule.setResource()` | 资源名（路由ID） | user-service-routes |

### 被限流时的响应

```json
{
    "code": 429,
    "message": "请求太频繁，请稍后再试"
}
```

---

## 第二级防护：order-service 服务间熔断降级

### SentinelRuleConfig.java

```java
@Configuration
public class SentinelRuleConfig {

    /**
     * 初始化流控规则
     * 限制对 user-service 的调用频率
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 限制 user-service 的 QPS 不超过 5
        FlowRule userRule = new FlowRule();
        userRule.setResource("GET:http://user-service/user/{id}");
        userRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRule.setCount(5);           // 每秒最多 5 次
        userRule.setLimitApp("default");
        rules.add(userRule);

        FlowRuleManager.loadRules(rules);
    }

    /**
     * 初始化熔断降级规则
     */
    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 慢调用比例熔断
        // 50% 的调用超过 500ms → 熔断 10 秒
        DegradeRule slowRule = new DegradeRule("GET:http://user-service/user/{id}");
        slowRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);       // 慢调用比例
        slowRule.setCount(500);                                  // 慢调用阈值：500ms
        slowRule.setSlowRatioThreshold(0.5);                     // 50% 慢调用
        slowRule.setTimeWindow(10);                              // 熔断 10 秒
        slowRule.setMinRequestAmount(5);                         // 最小请求数：5
        slowRule.setStatIntervalMs(5000);                        // 统计 5 秒
        rules.add(slowRule);

        // 异常比例熔断
        // 50% 异常 → 熔断 10 秒
        DegradeRule errorRule = new DegradeRule("GET:http://user-service/user/{id}");
        errorRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);  // 异常比例
        errorRule.setCount(0.5);                                          // 50% 异常
        errorRule.setTimeWindow(10);
        errorRule.setMinRequestAmount(5);
        errorRule.setStatIntervalMs(5000);
        rules.add(errorRule);

        DegradeRuleManager.loadRules(rules);
    }
}
```

### 流控规则详解

| 配置项 | 说明 | 本项目值 |
|--------|------|----------|
| `setResource()` | 资源名（与 Feign 接口路径对应） | `GET:http://user-service/user/{id}` |
| `setGrade()` | 流控维度（QPS/并发线程数） | `FLOW_GRADE_QPS` |
| `setCount()` | 阈值 | 5 QPS |

### 熔断规则详解

| 配置项 | 说明 | 本项目值 |
|--------|------|----------|
| `setGrade()` | 熔断策略 | `DEGRADE_GRADE_RT`（慢调用）/ `DEGRADE_GRADE_EXCEPTION_RATIO`（异常比例） |
| `setCount()` | 阈值 | 500ms（慢调用）/ 0.5（异常比例 50%） |
| `setSlowRatioThreshold()` | 慢调用比例阈值 | 0.5（50%） |
| `setTimeWindow()` | 熔断持续时长 | 10秒 |
| `setMinRequestAmount()` | 最小请求数 | 5 |
| `setStatIntervalMs()` | 统计时间窗口 | 5000ms |

### 熔断策略类型

| 策略 | 说明 | 配置 |
|------|------|------|
| `DEGRADE_GRADE_RT` | 慢调用比例 | 响应时间超过阈值视为慢调用 |
| `DEGRADE_GRADE_EXCEPTION_RATIO` | 异常比例 | 抛出异常的比例 |
| `DEGRADE_GRADE_EXCEPTION_COUNT` | 异常数 | 一段时间内的异常总数 |

### 熔断状态流转

```
              ┌──────────────────────────────┐
              │                              │
              ▼                              │
    ┌─────────────────┐                      │
    │   CLOSED        │                      │
    │   正常状态       │                      │
    └────────┬────────┘                      │
             │ 异常/慢调用比例                  │
             │ 达到阈值                        │
             ▼                              │
    ┌─────────────────┐                      │
    │   OPEN          │                      │
    │   熔断中        │ ──────────────────────┘
    │   所有请求降级   │   熔断时间窗口结束，
    └────────┬────────┘   尝试 Half-Open
             │ 熔断时间到达
             ▼
    ┌─────────────────┐
    │   HALF_OPEN     │
    │   尝试恢复       │
    │   放行一个请求   │
    └────────┬────────┘
              │
       ┌──────┴──────┐
       │ 成功        │ 失败
       ▼             ▼
   ┌─────────┐  ┌─────────┐
   │ CLOSED  │  │  OPEN   │
   │ 恢复    │  │ 重新熔断 │
   └─────────┘  └─────────┘
```

---

## 降级工厂：FallbackFactory

### 为什么用 FallbackFactory 而不是 Fallback？

| 对比 | Fallback | FallbackFactory |
|------|----------|-----------------|
| 能获取异常信息 | ❌ | ✅ |
| 根据异常类型做不同处理 | ❌ | ✅ |
| 方便日志记录 | 一般 | 更好 |

### UserClientFallbackFactory.java

```java
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        // 打印异常日志
        log.error("user-service 调用失败，触发降级。异常类型: {}, 异常信息: {}",
                cause.getClass().getSimpleName(), cause.getMessage());

        return new UserClient() {
            @Override
            public Map<String, Object> getUser(Long userId) {
                // 根据异常类型做差异化降级
                String reason = switch (cause.getClass().getSimpleName()) {
                    case "SocketTimeoutException" -> "用户服务响应超时";
                    case "ConnectException" -> "用户服务连接失败";
                    case "CircuitBreakerOpenException" -> "用户服务熔断器已打开";
                    default -> "用户服务暂时不可用";
                };

                log.warn("降级处理 - userId: {}, 降级原因: {}", userId, reason);

                return Map.of(
                        "code", 503,
                        "message", reason + "，请稍后重试",
                        "data", ""
                );
            }
        };
    }
}
```

### UserClient.java（Feign 接口）

```java
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    @GetMapping("/user/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long userId);
}
```

> ⚠️ **注意**：`fallbackFactory` 指定降级工厂类，由 `@Component` 注解自动注册。

### 降级响应示例

```json
{
    "code": 503,
    "message": "用户服务响应超时，请稍后重试",
    "data": ""
}
```

---

## 调用方处理降级响应

### OrderController.java

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    private final UserClient userClient;

    @GetMapping("/create")
    public Map<String, Object> createOrder(@RequestParam("userId") Long userId) {
        // 调用 user-service
        Map<String, Object> userInfo = userClient.getUser(userId);

        // 检查是否触发了降级（降级时 code=503）
        if (userInfo.containsKey("code") && Integer.valueOf(503).equals(userInfo.get("code"))) {
            return userInfo;  // 直接返回降级响应
        }

        // 正常逻辑...
        return Map.of("code", 200, "message", "订单创建成功");
    }
}
```

---

## 生产环境推荐做法

### 规则动态配置

本项目的规则是硬编码的（`SentinelRuleConfig`），生产环境推荐：

| 方式 | 优点 | 缺点 |
|------|------|------|
| **Nacos 配置中心** | 动态推送，无需重启 | 需要额外配置 |
| **Sentinel Dashboard** | 可视化配置 | 需要额外部署 |
| **代码配置** | 简单直接 | 修改需要重启服务 |

### Nacos 推送规则示例

```yaml
# Nacos 配置：sentinel-flow-rule.json
[
    {
        "resource": "GET:http://user-service/user/{id}",
        "limitApp": "default",
        "grade": 1,
        "count": 5,
        "controlBehavior": 0
    }
]
```

---

## 常见问题排查

### 1. 降级不生效

检查清单：
- [ ] `feign.sentinel.enabled: true` 是否配置
- [ ] `fallbackFactory` 是否指定正确
- [ ] FallbackFactory 是否被 `@Component` 注解
- [ ] `sentinel-transport` 端口是否被占用

### 2. 流控规则不生效

检查清单：
- [ ] 资源名是否与实际请求路径一致
- [ ] 规则是否正确加载（日志查看 `FlowRuleManager.loadRules`）
- [ ] 是否开启了 Sentinel `eagerness: true`

### 3. 熔断后不恢复

检查清单：
- [ ] `timeWindow` 是否设置过短（建议至少 5 秒）
- [ ] `minRequestAmount` 是否满足（熔断前需要积累足够样本）

---

## 总结

```
┌────────────────────────────────────────────────────────────────┐
│                    Sentinel 总结                                 │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  位置1：Gateway                                                 │
│  ├─ 作用：入口限流                                              │
│  ├─ 依赖：sentinel-spring-cloud-gateway-adapter                │
│  └─ 配置：GatewayFlowRule + BlockHandler                       │
│                                                                 │
│  位置2：order-service（服务间调用）                              │
│  ├─ 作用：保护 Feign 调用链                                     │
│  ├─ 依赖：spring-cloud-starter-alibaba-sentinel                │
│  ├─ 流控：FlowRule（限制 QPS）                                  │
│  ├─ 熔断：DegradeRule（慢调用/异常比例）                         │
│  └─ 降级：FallbackFactory                                       │
│                                                                 │
│  关键配置：                                                      │
│  └─ feign.sentinel.enabled: true                                │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

| 概念 | 说明 |
|------|------|
| **流控** | 限制 QPS，超出直接拒绝 |
| **熔断** | 检测异常比例/慢调用，自动熔断 |
| **降级** | 熔断期间调用 fallback 返回兜底数据 |
| **二级防护** | Gateway 挡大流量，服务内做精细控制 |
