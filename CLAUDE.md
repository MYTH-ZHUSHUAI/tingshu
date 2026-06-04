# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概要

听书（Tingshu）有声读物平台后端，基于 Spring Cloud Alibaba 微服务架构。

## 构建与运行

```bash
# 编译全部模块
mvn clean compile

# 打包全部模块（跳过测试）
mvn clean package -DskipTests

# 编译单个模块
mvn -pl service/service-album clean compile

# 运行单个服务（指定 Nacos 配置）
mvn -pl service/service-album spring-boot:run
```

本项目依赖 Nacos 作为注册中心和配置中心（`192.168.1.140:8848`），启动任何服务前需确保 Nacos 已运行。各服务的端口和中间件连接信息在 Nacos 配置中管理（参考 `资料/DEFAULT_GROUP/` 目录下的 YAML 文件）。`bootstrap.properties` 中仅配置服务名、profile 和 Nacos 地址。

## 技术栈

- **框架**: Spring Boot 3.0.5, Spring Cloud 2022.0.2, Spring Cloud Alibaba 2022.0.0.0-RC2
- **数据库**: MySQL 8.0.30 + MyBatis-Plus 3.5.3.1
- **缓存**: Redis + Redisson 3.20.0
- **消息队列**: RabbitMQ（延迟消息插件）、Kafka 3.1.0
- **注册中心 & 配置中心**: Nacos
- **网关**: Spring Cloud Gateway（端口 8500）
- **服务调用**: OpenFeign + Sentinel 熔断降级
- **对象存储**: MinIO、腾讯云 VOD
- **定时任务**: XXL-Job 2.4.0
- **API 文档**: Knife4j 4.1.0（OpenAPI 3）
- **Java 版本**: 17（必须使用 JDK 17，JDK 25+ 与 Lombok 1.18.26 不兼容，会导致 `@Slf4j` 等注解失效）

## 编译注意事项

- 系统 JDK 版本必须是 17，否则 Lombok 注解处理器无法生成 `log`、`getter/setter` 等代码
- 设置 JAVA_HOME 后再编译：
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 17)
  ```
- 编译单个模块时需同时编译 `model` 模块（其他模块依赖 model 中的实体类）：
  ```bash
  mvn -pl model,service/service-search clean compile
  ```

## 模块结构

```
tingshu-parent/
├── server-gateway/          # Spring Cloud Gateway 网关（8500）
├── common/                  # 公共模块
│   ├── common-util/         # 通用工具类（Result、MD5、AuthContextHolder 等）
│   ├── common-log/          # 操作日志注解和 AOP
│   ├── rabbit-util/         # RabbitMQ 消息发送封装
│   └── service-util/        # 服务公共配置（MyBatis-Plus、Redis、Knife4j、全局异常处理、Feign 拦截器）
├── model/                   # 共享实体、VO、Query 对象
├── service-client/          # Feign 客户端接口（含 Sentinel fallback 实现）
│   ├── service-album-client/
│   ├── service-user-client/
│   ├── service-account-client/
│   ├── service-order-client/
│   ├── service-search-client/
│   └── service-system-client/
└── service/                 # 业务服务模块
    ├── service-album/       # 专辑管理（8501）
    ├── service-user/        # 用户管理
    ├── service-account/     # 账户管理
    ├── service-order/       # 订单管理
    ├── service-payment/     # 支付管理
    ├── service-comment/     # 评论管理
    ├── service-dispatch/    # 调度服务（XXL-Job）
    ├── service-live/        # 直播服务（WebSocket）
    └── service-search/      # 搜索服务（Elasticsearch）
```

## 代码分层与命名约定

每个业务服务遵循固定分层：

- **controller/** — 管理端 Controller（由 Admin 前端调用）
- **api/** — Feign API Controller（供其他服务通过 Feign 调用），方法签名与 `service-client` 中的 Feign 接口保持一致
- **service/** — 业务接口 + **impl/** 实现类（继承 `ServiceImpl<Mapper, Entity>`）
- **mapper/** — MyBatis-Plus Mapper 接口

统一响应：`Result<T>`（`common-util`），成功用 `Result.ok(data)`，失败抛 `GuiguException`（`service-util`）。全局异常由 `GlobalExceptionHandler` 拦截处理。

## 关键模式

### 实体基类
所有数据库实体继承 `BaseEntity`，统一提供 `id`（自增主键）、`createTime`、`updateTime`、`isDeleted`（逻辑删除）字段。

### 消息队列
`RabbitService.sendMessage()` 发送消息时会自动封装 `GuiguCorrelationData`（含消息体、交换机、路由键），并将关联数据存入 Redis（10 分钟 TTL）用于重试确认。`MqConst` 中集中定义所有交换机、路由键、队列名称常量。

### 服务间调用
通过 OpenFeign 实现。`service-client` 模块定义 Feign 接口，接口的 `fallback` 指向同包下的 `impl/*DegradeFeignClient` 类。`FeignInterceptor` 自动从当前请求上下文传递 `token` 头到下游服务。

### 操作日志
`@Log` 注解（`common-log`）可记录操作日志，配合 AOP 切面 `LogAspect` 使用。

### 登录校验
`@TingshuLogin` 注解（`common/service-util`）标注在 Controller/Api 方法上，通过 AOP 切面 `TingshuLoginAspect` 校验登录态。切点覆盖所有 `com.atguigu.tingshu.*.api.*` 下的方法。`required=true`（默认）时强制登录，token 从请求头获取，用户信息存储在 `Redis` 中（key: `user:login:{token}`）。

### 搜索服务（service-search）
搜索服务不使用数据库，启动类排除 DataSource 自动配置：
```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
```
- **ES 索引初始化**: `EsIndexInitializer` 在启动时自动检查索引是否存在，不存在则按 `AlbumInfoIndex` 的 `@Field` 注解创建 mapping，确保 `nested` 等特殊类型正确
- **ES 查询**: 使用 `ElasticsearchClient`（ES 8.5.x Java Client），DSL 通过 Builder 模式拼装，不使用 Spring Data ES 的 `@Query` 注解
- **DSL 构建**: `SearchServiceImpl.buildQueryDsl()` 负责拼装查询条件、排序、高亮、分页；`parseSearchResult()` 负责解析响应并处理高亮字段
- **搜索设计原则**: keyword 必填、`must:match_all` + `should` 加分保证不返回空结果、`_score` 始终作为二级排序保留

### 线程池
异步操作使用 `@Resource` 注入 `ThreadPoolExecutor`，配置类在服务 `config/` 目录下定义 `@Bean`。例如搜索服务中 `upperAlbum` 使用 `albumUpperExecutor` 线程池并行调用多个 Feign 接口。
