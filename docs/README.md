# Spring Cloud 学习知识库

## 学习阶段总览

### 已完成阶段

| 阶段 | 主题 | 状态 |
|------|------|------|
| 1 | 父工程搭建 | ✅ |
| 2 | 服务注册与发现 - Eureka | ✅ |
| 3 | 服务注册与发现 - Nacos | ✅ |
| 4 | Nacos 配置中心 | ✅ |
| 5 | 服务调用与负载均衡 | ✅ |
| 6 | 服务熔断与降级 | ✅ |
| 7 | API 网关 | ✅ |
| 8 | 链路追踪 | ✅ |

> 📌 **当前策略**：先完整学习待学习组件（Seata/RocketMQ/XXL-JOB 等），后续新建独立项目进行实战。

### 待学习阶段（按优先级排序）

| 阶段 | 主题 | 优先级 | 状态 |
|------|------|--------|------|
| 9 | 分布式事务 - Seata | 🔥 高 | ⏳ |
| 10 | 消息队列 - RocketMQ | 🔥 高 | ⏳ |
| 11 | 分布式任务调度 - XXL-JOB | 🔥 高 | ⏳ |
| 12 | 监控告警 - Prometheus + Grafana | 🟡 中 | ⏳ |
| 13 | 日志收集 - ELK/EFK | 🟡 中 | ⏳ |
| 14 | API 文档 - Knife4j/Swagger | ⚪ 低 | ⏳ |
| 15 | 服务安全 - Spring Security OAuth2 | ⚪ 低 | ⏳ |

### 各阶段知识点

#### 已完成阶段

| 阶段 | 核心知识点 |
|------|------------|
| 1. 父工程搭建 | Maven 多模块、Spring Boot/Cloud/Alibaba 版本选型 |
| 2. Eureka | @EnableEurekaServer、服务注册、服务发现、自我保护机制 |
| 3. Nacos | Nacos 安装(Docker)、namespace、@EnableDiscoveryClient、服务注册 |
| 4. Nacos 配置中心 | bootstrap.yml、Profile 多环境(dev/pro)、动态配置刷新、@RefreshScope |
| 5. 服务调用与负载均衡 | OpenFeign、@FeignClient、LoadBalancer 轮询策略、OkHttp 底层客户端、超时配置 |
| 6. 服务熔断与降级 | Sentinel 流控、Sentinel 熔断、FallbackFactory 降级、Token 透传(FeignAuthInterceptor)、JWT 鉴权 |
| 7. API 网关 | Spring Cloud Gateway、Route/Predicate/Filter、lb://负载均衡路由、StripPrefix路径重写、GlobalFilter统一鉴权(JWT)、请求日志过滤器、CORS跨域配置、Sentinel网关限流 |
| 8. 链路追踪 | Micrometer Tracing、Zipkin Server、Brave 桥接、Actuator 自动配置、全链路追踪、冷启动问题分析、性能瓶颈定位 |

#### 待学习阶段

| 阶段 | 核心知识点 |
|------|------------|
| 9. 分布式事务 - Seata | Seata Server 搭建(Nacos注册)、AT 模式、@GlobalTransactional、回滚日志(undo_log)、分布式事务场景实践 |
| 10. 消息队列 - RocketMQ | RocketMQ 安装、生产者/消费者模型、消息可靠性保障、延迟消息、顺序消息、Spring Boot 集成 |
| 11. 分布式任务调度 - XXL-JOB | XXL-JOB Admin 控制台、执行器注册、Cron 表达式、分片广播、失败重试、定时任务场景实践 |
| 12. 监控告警 - Prometheus + Grafana | Micrometer 指标暴露、Prometheus 数据抓取、Grafana 可视化大盘、告警规则配置 |
| 13. 日志收集 - ELK/EFK | Logback 配置、Logstash 日志收集、Elasticsearch 存储、Kibana 查询、Trace ID 日志关联 |
| 14. API 文档 - Knife4j/Swagger | Swagger 注解、Knife4j 增强 UI、接口分组、参数校验文档 |
| 15. 服务安全 - Spring Security OAuth2 | OAuth2 授权码模式、JWT Token 签发、资源服务器配置、微服务间安全调用 |

---

## 当前版本配置

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>
</properties>
```

---

## 常用命令

### Maven
```powershell
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 打包项目
mvn clean package
```

### Nacos (Docker)
```powershell
# 启动 Nacos
docker run -d --name nacos -p 8848:8848 -p 9848:9848 nacos/nacos-server:v2.2.3

# 停止 Nacos
docker stop nacos

# 删除 Nacos
docker rm nacos
```

---

## 常见端口

| 服务 | 端口 |
|------|------|
| Eureka Server | 7001 |
| Nacos | 8848 |
| user-service | 8081 |
| order-service | 8082 |
| gateway | 8080 |

---

## 项目结构

```
springcloud-learn/
├── pom.xml
├── eureka-server/          # Eureka 注册中心
├── user-service/          # 用户服务
├── order-service/          # 订单服务（Feign + Sentinel）
├── gateway/               # API 网关（Gateway + 鉴权 + 日志 + CORS + Sentinel限流）
└── docs/
    ├── README.md           # 学习总览
    ├── 02-Eureka.md        # Eureka 学习笔记
    ├── 03-Nacos.md         # Nacos 学习笔记
    ├── 04-OpenFeign.md     # OpenFeign 学习笔记
    └── 05-Gateway.md       # Gateway 学习笔记
```

---

## 资料链接

- Spring Cloud 官方文档: https://spring.io/projects/spring-cloud
- Spring Cloud Netflix Eureka: https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/
- Nacos 官方文档: https://nacos.io/zh-cn/docs/quick-start.html
- Spring Cloud Alibaba: https://spring-cloud-alibaba.github.io/
