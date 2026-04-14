# Spring Cloud 学习知识库

## 学习阶段总览

| 阶段 | 主题 | 状态 |
|------|------|------|
| 1 | 父工程搭建 | ✅ |
| 2 | 服务注册与发现 - Eureka | ✅ |
| 3 | 服务注册与发现 - Nacos | ✅ |
| 4 | Nacos 配置中心 | ✅ |
| 5 | 服务调用与负载均衡 | ⏳ |
| 6 | 服务熔断与降级 | ⏳ |
| 7 | API 网关 | ⏳ |
| 8 | 链路追踪 | ⏳ |

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
└── docs/
    ├── README.md
    └── 02-Eureka.md
```

---

## 资料链接

- Spring Cloud 官方文档: https://spring.io/projects/spring-cloud
- Spring Cloud Netflix Eureka: https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/
- Nacos 官方文档: https://nacos.io/zh-cn/docs/quick-start.html
- Spring Cloud Alibaba: https://spring-cloud-alibaba.github.io/
