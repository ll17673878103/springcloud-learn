# 06-链路追踪(Zipkin)

## 学习目标

- 理解链路追踪的概念和作用
- 掌握 Spring Boot 3.x 集成 Zipkin 的方法
- 能够使用 Zipkin UI 分析调用链路
- 能够定位性能瓶颈和排查故障

---

## 什么是链路追踪

在微服务架构中，一个请求往往会经过多个服务：

```
用户请求 → Gateway → order-service → user-service
```

当系统出现问题（请求慢、报错），很难快速定位是哪个环节出了问题。**链路追踪**就是用来记录一个请求经过了哪些服务、每个环节花了多少时间。

---

## 核心概念

| 概念 | 简单理解 | 比喻 |
|------|---------|------|
| **Trace** | 一次完整的请求链路 | 一首完整的曲子 |
| **Span** | 链路中的一个环节 | 曲子里的一个小节 |
| **Service** | 参与的微服务 | 演奏的乐器 |

---

## 版本选型

本项目使用：

```xml
<!-- 父 POM 版本管理 -->
<spring-boot.version>3.2.0</spring-boot.version>
<spring-cloud.version>2023.0.0</spring-cloud.version>
```

**Spring Boot 3.x 集成 Zipkin 使用 Micrometer Tracing**：

- `micrometer-tracing-bridge-brave`：Brave 桥接
- `zipkin-reporter-brave`：Zipkin Reporter

---

## 完整集成步骤

### 第一步：父 POM 配置

```xml
<properties>
    <micrometer-tracing.version>1.2.0</micrometer-tracing.version>
    <zipkin-reporter.version>2.16.3</zipkin-reporter.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
            <version>${micrometer-tracing.version}</version>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
            <version>${zipkin-reporter.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> ⚠️ **重要**：groupId 必须是 `io.zipkin.reporter2`，不是 `iio.zipkin.reporter2`（常见拼写错误）。

### 第二步：子模块依赖

在每个需要链路追踪的服务（gateway、order-service、user-service）的 `pom.xml` 中添加：

```xml
<dependencies>
    <!-- 必须：Actuator 提供自动配置 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- 链路追踪桥接 -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
    
    <!-- Zipkin 数据上报 -->
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>
</dependencies>
```

> ⚠️ **关键**：必须加 `spring-boot-starter-actuator`！`management.tracing` 和 `management.zipkin` 的自动配置类来自 `spring-boot-actuator-autoconfigure`，没有 actuator，Zipkin Reporter 的 Bean 不会被创建。

### 第三步：配置文件

在 `application.yml` 中添加：

```yaml
management:
  tracing:
    sampling:
      probability: 1.0    # 采样率：1.0 = 100% 采样（学习阶段），生产环境建议 0.1
  zipkin:
    tracing:
      endpoint: http://101.43.103.32:9411/api/v2/spans  # Zipkin Server 地址
```

### 第四步：调试日志（可选）

在 `application.yml` 中开启 debug 日志：

```yaml
logging:
  level:
    io.micrometer.tracing: debug
    zipkin2: debug
    brave: debug
```

### 第五步：重启服务

1. **刷新 Maven**（IDEA Maven 面板点刷新按钮）
2. **Rebuild Project**
3. 重启服务
4. 启动日志中搜索 `ZipkinAutoConfiguration`，确认自动配置生效

---

## 启动 Zipkin Server

Zipkin Server 需要单独启动：

```bash
# 方式1：直接运行 jar
java -jar zipkin-server-3.x.x-exec.jar

# 方式2：Docker
docker run -d -p 9411:9411 openzipkin/zipkin
```

启动后访问 `http://localhost:9411` 或 `http://101.43.103.32:9411` 查看 UI。

---

## Zipkin UI 使用指南

### 搜索界面

打开 Zipkin UI，会看到：

- **Service Name**：选择服务（如 `gateway`、`order-service`）
- **Span Name**：选择接口（如 `GET /order/create`）
- 点 **RUN QUERY** 搜索

### 查看链路详情

搜索结果会显示每个请求的耗时。点击右侧的 **SHOW** 按钮进入详情：

```
┌─ gateway (总耗时 15ms)
│
└─ order-service (/order/create)  ← Feign 远程调用
     └─ user-service (/user/{id}) ← 被调用服务
```

### 看什么？（四个维度）

**1. 时间瀑布图（最重要的）**

```
gateway          ████████████████████████████  750ms
  order-service       ████████████            ~500ms
    user-service          ████████            ~300ms
```

每个色块的长度代表耗时。一眼就能看出**哪个服务慢**。

**2. 调用关系**

谁调了谁，一目了然：
- 通过**网关**请求：`gateway → order-service → user-service`（三层）
- **直连** order-service：`order-service → user-service`（两层）

**3. 标签/Tags**

点开某个 Span，能看到详细信息：
- `http.method`: GET
- `http.url`: /order/create
- `http.status_code`: 200
- `ip`: 具体机器 IP

**4. 错误定位**

如果某个请求报错了，Zipkin 里会**标红**，一眼就能看到是哪个服务出了问题，比翻日志快多了。

---

## 首次请求 vs 后续请求

### 第一次请求（冷启动）

```
gateway: 750ms
  order-service: 413ms
```

**慢的原因**：
- Nacos 服务发现：第一次拉取服务列表
- Feign 连接池：第一次建立连接
- Sentinel 初始化：熔断器首次加载

### 后续请求

```
gateway: 15ms
  order-service: 7.5ms
```

**快了 48 倍！** 所有连接和初始化都已完成。

---

## 实战排查示例

### 场景：接口变慢

**现象**：`/order/create` 从 15ms 变成 500ms

**排查步骤**：
1. 打开 Zipkin，搜索最近失败的请求
2. 点 SHOW 看时间瀑布图
3. 发现 `user-service` 的 span 占了 450ms
4. 定位到 user-service 有问题（数据库慢查询、Redis 超时等）

### 场景：请求失败

**现象**：接口报 500 错误

**排查步骤**：
1. Zipkin 里搜索 `outcome: FAILURE`
2. 标红的 span 就是出错的地方
3. 点进去看 `error` tag，获取异常信息
4. 用 Trace ID 去日志里搜索完整堆栈

---

## 常见坑

| 问题 | 原因 | 解决 |
|------|------|------|
| 配置正确但 Zipkin 没记录 | 缺少 `spring-boot-starter-actuator` | 三个服务都加上 |
| 依赖下载失败 | `groupId` 拼成 `iio.zipkin.reporter2` | 改为 `io.zipkin.reporter2` |
| 启动后没记录 | 没刷新 Maven + Rebuild | 刷新后重启 |
| Zipkin UI 打不开 | Zipkin Server 没启动 | 先启动 Server |
| 链路不完整 | 只配置了部分服务 | 网关 + 所有微服务都要配置 |

---

## 总结

### 集成链路追踪的必要性

- **快速定位瓶颈**：一眼看出哪个服务慢
- **故障排查**：不用翻几十个服务的日志
- **性能优化**：对比首次/后续请求，发现冷启动问题
- **调用关系可视化**：理解系统架构

### 三个关键点

1. **groupId 不能拼错**：`io.zipkin.reporter2`（不是 `iio`）
2. **必须加 actuator**：没有它自动配置不生效
3. **改完依赖要刷新 Maven + Rebuild**

### 后续扩展

- 自定义 Span：在代码中手动添加业务追踪点
- 链路日志关联：在日志中输出 Trace ID，方便搜索
- 告警集成：基于链路追踪数据配置告警规则

---

## 参考资料

- Micrometer Tracing 官方文档：https://docs.micrometer.io/micrometer/reference/tracing.html
- Zipkin 官方文档：https://zipkin.io/
- Brave 项目：https://github.com/openzipkin/brave
