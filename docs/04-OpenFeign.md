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

## 步骤 1：给 order-service 添加依赖

修改 `order-service/pom.xml`：

```xml
<dependencies>
    <!-- 保留 web 依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 新增：Nacos Discovery（服务发现） -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- 新增：OpenFeign（服务调用） -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- 新增：LoadBalancer（负载均衡） -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
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
        server-addr: 127.0.0.1:8848
        namespace: public
```

**注意**：如果 Nacos 使用了自定义 namespace，需要改成对应的 namespace ID

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

/**
 * Feign 客户端接口
 * 声明式调用 user-service 的 API
 */
@FeignClient(name = "user-service")  // 指定要调用的服务名
public interface UserClient {

    /**
     * 调用 user-service 的 /user/{id} 接口
     * 注意：路径要和 user-service 保持一致
     */
    @GetMapping("/user/{id}")
    String getUser(@PathVariable("id") Long id);
}
```

---

## 步骤 5：创建 Order 控制器

在 `order-service` 中创建 `controller/OrderController.java`：

```java
package com.learn.orderservice.controller;

import com.learn.orderservice.feign.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserClient userClient;

    /**
     * 创建订单接口
     * 演示通过 Feign 调用 user-service 获取用户信息
     */
    @GetMapping("/create")
    public String createOrder(@RequestParam("userId") Long userId) {
        log.info("创建订单，用户ID: {}", userId);

        // 通过 Feign 调用 user-service 获取用户信息
        String userInfo = userClient.getUser(userId);

        return "订单创建成功！用户信息：" + userInfo;
    }
}
```

---

## 步骤 6：启动服务测试

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

## 扩展：传递 Header 信息

如果需要传递请求头（如 token）：

```java
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/user/{id}")
    String getUser(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String token  // 传递 Header
    );
}
```

---

## 下阶段预告

- **第五阶段：服务熔断与降级** - 使用 Sentinel 保护服务

---

## 本章小结

| 概念 | 说明 |
|------|------|
| **OpenFeign** | 声明式 HTTP 客户端，像调用本地方法一样调用远程服务 |
| **@FeignClient** | 标记要调用的服务名 |
| **LoadBalancer** | 客户端负载均衡，自动选择服务实例 |
| **服务发现** | 通过 Nacos 获取服务地址，无需硬编码 IP |
