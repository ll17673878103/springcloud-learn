# 第四阶段：服务调用与负载均衡

## 本阶段目标

- 理解为什么微服务之间需要远程调用
- 掌握 OpenFeign 实现服务调用
- 理解负载均衡的概念（客户端 vs 服务端）
- 掌握 Ribbon/LoadBalancer 负载均衡策略

---

## 为什么需要服务调用？

### 场景举例

```
用户下单流程：

1. 用户请求 order-service（创建订单）
         ↓
2. order-service 需要查询用户信息
         ↓
3. order-service 调用 user-service 获取用户数据
         ↓
4. 返回订单确认给用户
```

如果不用服务调用……你要这样写：

```java
// ❌ 错误示例：直接拼接 URL
String url = "http://localhost:8081/user/" + userId;
RestTemplate template = new RestTemplate();
User user = template.getForObject(url, User.class);
```

**问题**：
- `localhost:8081` 是写死的！服务部署到其他机器就凉了
- 不知道 user-service 有几个实例
- 没法做负载均衡

### 正确方式

```
order-service 调用 user-service（通过服务名）
         ↓
   Nacos 注册中心帮忙找到真实地址
         ↓
   负载均衡选择一个实例
         ↓
   返回结果
```

---

## OpenFeign 是什么？

### Feign vs OpenFeign

| 组件 | 说明 |
|------|------|
| **Feign** | Netflix 开发的声明式 HTTP 客户端 |
| **OpenFeign** | Spring Cloud 对 Feign 的封装，更易用 |

**核心思想**：像调用本地方法一样调用远程服务

```
// 就像这样写接口
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/user/{id}")
    User getUser(@PathVariable("id") Long id);
}

// 调用的时候就像调用本地方法
User user = userClient.getUser(1L);
```

---

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    服务调用架构                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────┐                                           │
│   │    用户     │                                           │
│   └──────┬──────┘                                           │
│          │                                                  │
│          ▼                                                  │
│   ┌─────────────┐                                           │
│   │ order-service │ ←── OpenFeign Client                    │
│   └──────┬──────┘                                           │
│          │  调用 user-service                               │
│          ▼                                                  │
│   ┌─────────────┐     ┌─────────────┐                        │
│   │ user-service │ ←── │ user-service │ (多实例)             │
│   │   :8081     │     │   :8082     │                        │
│   └─────────────┘     └─────────────┘                        │
│          ▲                                                  │
│          │                                                  │
│   ┌──────┴──────┐                                           │
│   │    Nacos   │ ←── 服务注册与发现                         │
│   └─────────────┘                                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘

负载均衡策略（轮询/随机/权重）：
   order-service 调用时，LoadBalancer 自动选择实例
```

---

## 项目结构

```
springcloud-learn/
├── order-service/              # 订单服务（需要新增 Feign）
│   └── pom.xml
└── user-service/              # 用户服务（已有）
    └── src/main/java/...
```

---

## 关键注解：@EnableDiscoveryClient

### 它是什么？

`@EnableDiscoveryClient` 是 Spring Cloud 提供的通用注解，作用是：**让当前服务注册到注册中心，并能发现其他服务。**

```java
@SpringBootApplication
@EnableDiscoveryClient  // 告诉注册中心："我来了，这是我的名字和地址"
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### 注册的前提条件

一个服务要注册到注册中心，需要同时满足三个条件：

| 条件 | 说明 | 示例 |
|------|------|------|
| ① 客户端依赖 | pom.xml 中引入注册中心依赖 | `spring-cloud-starter-alibaba-nacos-discovery` |
| ② 启用注解 | 启动类上加 `@EnableDiscoveryClient` | 见上方代码 |
| ③ 配置地址 | 配置文件中指定注册中心地址 | `bootstrap-dev.yml` 中配 Nacos 地址 |

三个条件缺一不可。

### @EnableDiscoveryClient vs @EnableEurekaClient

| 注解 | 注册中心 | 特点 |
|------|---------|------|
| `@EnableDiscoveryClient` | **通用**（Nacos、Eureka、Consul…） | ✅ 推荐使用，切换注册中心不用改代码 |
| `@EnableEurekaClient` | 仅 Eureka | Eureka 专用，功能一样 |

> **最佳实践**：推荐使用 `@EnableDiscoveryClient`，因为它与具体注册中心解耦。
>
> 从 Spring Cloud Edgware 版本开始，如果 classpath 上有注册中心客户端依赖，服务会**自动注册**，
> 注解可以不写。但写上是好习惯——明确告诉阅读者"这个服务要注册到注册中心"。

---

## 所有服务都需要注册吗？

**不是的。** 只有需要被其他服务"发现"的服务才需要注册。

### 应该注册的服务

| 服务类型 | 注册？ | 原因 |
|---------|--------|------|
| 服务提供者（如 `user-service`） | ✅ | 要让别人找到它 |
| 服务消费者（如 `order-service`） | ✅ | 既调用别人，也可能被别人调用 |
| API 网关（如 Spring Cloud Gateway） | ✅ | 需要从注册中心拉取服务列表做路由 |

### 不需要注册的服务

| 服务类型 | 注册？ | 原因 |
|---------|--------|------|
| 纯前端应用（Vue/React） | ❌ | 不参与微服务间调用 |
| 定时任务/批处理服务 | ❌ | 只调别人，不需要被别人调 |
| 注册中心本身（Eureka/Nacos） | ❌ | 自己不能注册给自己（Eureka 特殊机制除外） |
| 数据库、Redis、MQ 等中间件 | ❌ | 不是 Spring Cloud 应用 |
| 配置中心（如 Nacos Server） | ❌ | 本身就是基础设施 |

### 注册中心本身需要做成微服务模块吗？

| 注册中心 | 部署方式 | 需要单独模块？ |
|---------|---------|-------------|
| Eureka | 自己写 Spring Boot 项目启动 | ✅ 需要（如项目中的 `eureka-server`） |
| Nacos | 下载安装包，独立运行 | ❌ **不需要** |
| Consul | 下载二进制，独立运行 | ❌ 不需要 |

> Nacos 就像 MySQL、Redis 一样，是基础设施，直接部署运行即可。
> 不需要用 Spring Boot 再包一层。

---

## 步骤 1：给 order-service 添加依赖

修改 `order-service/pom.xml`：

```xml
<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Nacos Discovery（服务发现） -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- OpenFeign（服务调用） -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- LoadBalancer（负载均衡） -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>

    <!-- Sentinel（熔断降级） -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>

    <!-- JWT（Token 解析） -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>

    <!-- OkHttp（Feign 底层客户端，连接池性能更好） -->
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

---

## 步骤 2：添加配置文件

创建 `order-service/src/main/resources/application.yml`：

```yaml
server:
  port: 8082

spring:
  application:
    name: order-service
  cloud:
    nacos:
      discovery:
        server-addr: 101.43.103.32:8848
        namespace: c8086219-54ec-4b09-bcc3-1b13193f770e
    # OpenFeign 超时配置
    openfeign:
      client:
        config:
          # 全局默认配置
          default:
            connect-timeout: 3000    # 连接超时 3秒
            read-timeout: 5000       # 读取超时 5秒
          # 针对特定服务的配置（优先级高于 default）
          user-service:
            connect-timeout: 2000    # user-service 连接超时 2秒
            read-timeout: 3000       # user-service 读取超时 3秒
      # 底层客户端替换为 OkHttp（连接池，性能更好）
      okhttp:
        enabled: true

# Sentinel 配置
sentinel:
  transport:
    dashboard: localhost:8080    # Sentinel Dashboard 地址（可选）
    port: 8719                  # 与 Dashboard 通信的端口
  eagerness: true               # 启动时立即初始化 Sentinel

feign:
  sentinel:
    enabled: true               # 开启 Feign 对 Sentinel 的支持

# Feign 日志级别
logging:
  level:
    com.learn.orderservice.feign: debug
```

**配置说明**：
- `openfeign.client.config`：支持全局（default）和按服务名配置超时
- `okhttp.enabled: true`：替换默认的 URLConnection 为 OkHttp，支持连接池
- `feign.sentinel.enabled: true`：开启 Sentinel 对 Feign 的熔断降级支持

---

## 步骤 3：启用 Feign 客户端

修改 `OrderServiceApplication.java`：

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients  // 启用 Feign 客户端扫描
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

---

## 步骤 4：创建 User 服务客户端

在 `order-service` 中创建包 `feign`，新建 `UserClient.java`：

```java
package com.learn.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign 客户端接口
 * 声明式调用 user-service 的 API
 *
 * fallbackFactory vs fallback：
 * - fallback：只能兜底，拿不到异常信息
 * - fallbackFactory：可以捕获具体异常，根据异常类型做不同处理（推荐）
 */
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    /**
     * 调用 user-service 的 /user/{id} 接口
     * 该接口需要 Token 鉴权
     */
    @GetMapping("/user/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long userId);
}
```

**关键变化**：`fallbackFactory = UserClientFallbackFactory.class` 指定了降级工厂

---

## 步骤 5：创建降级工厂

在 `order-service` 中创建 `feign/UserClientFallbackFactory.java`：

```java
package com.learn.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * UserClient 降级工厂（推荐方式）
 *
 * 优势：
 * 1. 可以捕获具体的异常信息（超时、拒绝连接、500 等）
 * 2. 根据不同异常类型做不同的降级处理
 * 3. 更方便记录日志和排查问题
 */
@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("user-service 调用失败，触发降级。异常类型: {}, 异常信息: {}",
                cause.getClass().getSimpleName(), cause.getMessage());

        return new UserClient() {
            @Override
            public Map<String, Object> getUser(Long userId) {
                // 根据异常类型做差异化降级
                String reason = switch (cause.getClass().getSimpleName()) {
                    case "SocketTimeoutException" -> "用户服务响应超时";
                    case "ConnectException" -> "用户服务连接失败（服务可能已下线）";
                    case "CircuitBreakerOpenException" -> "用户服务熔断器已打开";
                    default -> "用户服务暂时不可用";
                };

                log.warn("降级处理 - userId: {}, 降级原因: {}", userId, reason);

                return Map.of(
                        "code", 503,
                        "message", reason + "，请稍后重试",
                        "data", ""      // 降级时 data 置空，不返回假数据
                );
            }
        };
    }
}
```

> **降级规范**：`data` 字段置空（`""`），不返回假数据。前端可以根据 `code=503` 判断是降级响应。

---

## 步骤 6：创建 Token 透传拦截器

在 `order-service` 中创建 `interceptor/FeignAuthInterceptor.java`：

```java
package com.learn.orderservice.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * 核心功能：自动将当前请求的 Authorization Header 转发给下游服务
 *
 * 工作原理：
 * 1. 用户请求 order-service 时带了 Token
 * 2. order-service 通过 Feign 调用 user-service
 * 3. 这个拦截器在 Feign 发请求前，自动把 Token 塞到转发请求的 Header 里
 * 4. user-service 的 AuthInterceptor 就能验证这个 Token
 */
@Slf4j
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 1. 获取当前 HTTP 请求上下文
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.warn("无法获取当前请求上下文，跳过 Token 传递");
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 2. 从当前请求中取出 Authorization Header
        String authorization = request.getHeader("Authorization");

        if (authorization != null && !authorization.isEmpty()) {
            // 3. 将 Token 传递给 Feign 的转发请求
            template.header("Authorization", authorization);
            log.debug("Feign 请求已携带 Authorization Header");
        }
    }
}
```

**Token 透传流程**：

```
用户请求（带 Token）
    → order-service（OrderController）
        → FeignAuthInterceptor（自动把 Token 塞到 Header）
            → Feign 调用 user-service（带 Token）
                → user-service AuthInterceptor（验证 Token）
                    → 返回用户数据
```

---

## 步骤 7：创建 Order 控制器

在 `order-service` 中创建 `controller/OrderController.java`：

```java
package com.learn.orderservice.controller;

import com.learn.orderservice.feign.UserClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final UserClient userClient;

    @GetMapping("/create")
    public Map<String, Object> createOrder(
            @RequestParam("userId") Long userId,
            HttpServletRequest request) {

        log.info("创建订单，用户ID: {}", userId);

        // 通过 Feign 调用 user-service 获取用户信息
        // FeignAuthInterceptor 会自动传递 Token
        Map<String, Object> userInfo = userClient.getUser(userId);

        // 检查是否触发了降级（降级时 code=503）
        if (userInfo.containsKey("code") && Integer.valueOf(503).equals(userInfo.get("code"))) {
            return userInfo;
        }

        return Map.of(
                "code", 200,
                "message", "订单创建成功",
                "data", Map.of(
                        "orderId", System.currentTimeMillis(),
                        "userId", userId,
                        "userInfo", userInfo
                )
        );
    }
}
```

---

## 步骤 8：配置 Sentinel 熔断规则

在 `order-service` 中创建 `config/SentinelRuleConfig.java`：

```java
package com.learn.orderservice.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class SentinelRuleConfig {

    /**
     * 流控规则：限制 QPS
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule userRule = new FlowRule();
        userRule.setResource("GET:http://user-service/user/{id}");
        userRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRule.setCount(10);  // 每秒最多 10 次请求
        userRule.setLimitApp("default");
        rules.add(userRule);

        FlowRuleManager.loadRules(rules);
    }

    /**
     * 熔断降级规则
     */
    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 慢调用比例：50% 超过 1 秒 → 熔断 10 秒
        DegradeRule slowRule = new DegradeRule("GET:http://user-service/user/{id}");
        slowRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        slowRule.setCount(1000);
        slowRule.setSlowRatioThreshold(0.5);
        slowRule.setTimeWindow(10);
        slowRule.setMinRequestAmount(5);
        slowRule.setStatIntervalMs(10000);
        rules.add(slowRule);

        // 异常比例：50% 异常 → 熔断 10 秒
        DegradeRule errorRule = new DegradeRule("GET:http://user-service/user/{id}");
        errorRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        errorRule.setCount(0.5);
        errorRule.setTimeWindow(10);
        errorRule.setMinRequestAmount(5);
        errorRule.setStatIntervalMs(10000);
        rules.add(errorRule);

        DegradeRuleManager.loadRules(rules);
    }
}
```

> **注意**：生产环境中，规则通常通过 Sentinel Dashboard 或 Nacos 动态推送，不需要写代码。
> 这里用代码配置是为了学习和本地测试方便。

---

## 步骤 9：启动服务测试

### 1. 启动 Nacos
```powershell
docker start nacos
```

### 2. 启动 user-service
确保 user-service 已注册到 Nacos

### 3. 启动 order-service
启动后检查 Nacos 控制台，确认 order-service 也已注册

### 4. 测试调用
```powershell
# 访问 order-service，它会调用 user-service
curl http://localhost:8082/order/create?userId=1
```

**预期结果**：返回订单创建成功，并包含从 user-service 获取的用户信息

---

## 负载均衡策略

### 默认策略：轮询（Round Robin）

```
第1次请求 → user-service:8081
第2次请求 → user-service:8082
第3次请求 → user-service:8081
第4次请求 → user-service:8082
...
```

### 其他常用策略

| 策略 | 说明 | 配置 |
|------|------|------|
| **轮询** | 依次调用每个实例 | 默认 |
| **随机** | 随机选择一个实例 | random |
| **权重** | 按权重比例分配 | weight=xxx |
| **最少连接** | 选择连接数最少的 | leост |

### 切换负载均衡策略

**方式一：配置文件**
```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false  # 禁用 Ribbon，使用 LoadBalancer
```

**方式二：Java 配置类**
```java
@Configuration
public class LoadBalancerConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory factory) {
        String name = environment.getProperty(
            SpringCloudLoadBalancerClientFactory.LOADBALANCER_CONFIG_PROPERTY_NAME);

        return new RandomLoadBalancer(
            factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }
}
```

---

## 常见问题

### Q1: 启动报错 "No Feign Client for name..."

**原因**：服务名拼写错误，或服务未注册到 Nacos

**解决**：
1. 检查 Nacos 控制台，确认服务名正确
2. 检查 `spring.application.name` 配置
3. 检查 order-service 是否注册成功

### Q2: 调用超时

**原因**：user-service 响应太慢

**解决**：添加超时配置
```yaml
feign:
  client:
    config:
      default:
        connect-timeout: 5000      # 连接超时 5秒
        read-timeout: 10000        # 读取超时 10秒
```

### Q3: 服务名大小写问题

**注意**：Nacos 默认不区分大小写，但建议保持一致

---

## 扩展：Feign 底层客户端替换

默认 Feign 使用 `java.net.HttpURLConnection`，不支持连接池。可以替换为 OkHttp 提升性能。

**只需两步**：

1. 添加依赖（已在步骤 1 中引入 `feign-okhttp`）
2. 配置启用：

```yaml
spring:
  cloud:
    openfeign:
      okhttp:
        enabled: true
```

> OkHttp 支持连接池、GZIP 压缩、HTTP/2，性能比默认的 URLConnection 好很多。

---

## 知识总结：熔断 vs 降级 vs 流控

这三个概念容易混淆，来理清一下：

| 概念 | 是什么 | 谁来做 | 项目中的体现 |
|------|--------|--------|------------|
| **流控** | 限制请求频率，防止流量打垮服务 | Sentinel | `SentinelRuleConfig.initFlowRules()` |
| **熔断** | 错误率/慢调用达到阈值，断开调用链路 | Sentinel | `SentinelRuleConfig.initDegradeRules()` |
| **降级** | 调用失败后的兜底方案，返回友好提示 | FallbackFactory | `UserClientFallbackFactory` |

**三者的关系**：

```
请求进入
  ↓
流控检查 → 超过 QPS 限制？→ 直接拒绝（降级）
  ↓ 通过
发起远程调用
  ↓
熔断器状态？
  ├─ 关闭 → 正常调用
  ├─ 打开 → 不调用，直接降级
  └─ 半开 → 放一个请求试试，成功就关熔断器
  ↓
调用失败（超时/异常）→ 触发降级
```

---

## 常见踩坑记录

### 坑 1：OpenFeign 降级不生效

**原因**：Feign 的降级需要熔断器（Sentinel/Resilience4j）支持，单独配置 `fallbackFactory` 不会生效。

**解决**：
```yaml
feign:
  sentinel:
    enabled: true  # 必须开启！
```

### 坑 2：FallbackFactory 与旧 Fallback 类冲突

**原因**：同时配置 `fallback` 和 `fallbackFactory` 会导致 Bean 冲突。

**解决**：只用 `fallbackFactory`，删掉旧的 `fallback` 类。

### 坑 3：Spring Cloud 与 Alibaba 版本不兼容

**原因**：Spring Cloud、Spring Cloud Alibaba、Spring Boot 三者版本必须匹配。

**解决**：参考[官方版本对照表](https://spring-cloud-alibaba.github.io/)，本项目使用：
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Alibaba 2022.0.0.0

---

## 下阶段预告

- **第七阶段：API 网关** - 使用 Spring Cloud Gateway 统一入口

---

## 本章小结

| 概念 | 说明 |
|------|------|
| **OpenFeign** | 声明式 HTTP 客户端，像调用本地方法一样调用远程服务 |
| **@FeignClient** | 标记要调用的服务名，可指定 fallbackFactory |
| **@EnableDiscoveryClient** | 通用服务注册发现注解，支持 Nacos/Eureka/Consul |
| **LoadBalancer** | 客户端负载均衡，自动选择服务实例 |
| **OkHttp** | Feign 底层客户端替换，支持连接池，性能更好 |
| **FallbackFactory** | Feign 降级工厂，可捕获异常做差异化降级（推荐） |
| **FeignAuthInterceptor** | Feign 请求拦截器，自动透传 Token |
| **Sentinel 流控** | 限制 QPS，防止流量打垮服务 |
| **Sentinel 熔断** | 错误率/慢调用达阈值自动断开调用链路 |
| **服务降级** | 调用失败后的兜底方案，data 置空不返回假数据 |
| **选择性注册** | 只有需要被其他服务"发现"的服务才注册到注册中心 |
