# Effi-RPC Project Mastery

本文档用于长期维护、快速恢复上下文和模块级记忆恢复。
范围覆盖除 `effi-rpc-integration/effi-rpc-spring-boot-starter` 外的全部模块；Spring Boot starter 只作为外部集成边界记录，不展开内部实现。

## 1. 项目定位

Effi-RPC 是一个面向接口和注解的 RPC 框架，核心特点：

- 统一 `Caller` / `Servant` 抽象，调用方和服务方使用同一套上下文模型。
- `Platform -> Application -> Module` 三级作用域上下文。
- 通过 `@Extension`、`@Extensible`、`@ScopedComponent` 提供扩展和组件装载机制。
- `@Call` / `@CallGroup` / `@Serve` / `@ServeGroup` 提供声明式 RPC 配置。
- 传输层抽象为 `transport-api`，默认实现为 Netty。
- 协议层支持 HTTP/1、HTTP/2 和 gRPC 骨架。
- 注册中心支持 Consul 和 Nacos。
- 序列化支持 JDK、Jackson、Kryo、Protobuf；压缩支持 GZIP、Deflate、LZ4、Snappy。
- 注解处理器生成扩展元数据、作用域组件元数据和 GraalVM native-image 配置。

## 2. 构建与验证

- 仓库根目录：`C:\Users\zhouwenbo\Desktop\rpc\code\effi-rpc`
- JDK：`C:\dev\Java\jdk-25.0.3`
- Gradle wrapper：`gradlew.bat`
- Gradle 版本：`9.5.1`
- Gradle 用户目录：`D:\tools\gradle`

典型验证命令：

```powershell
$env:TEMP = 'C:\Users\zhouwenbo\Desktop\rpc\code\effi-rpc\.gradle\tmp'
$env:TMP = $env:TEMP
$env:GRADLE_USER_HOME = 'D:\tools\gradle'
$env:JAVA_HOME = 'C:\dev\Java\jdk-25.0.3'

.\gradlew.bat :effi-rpc-common:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :effi-rpc-test:test --no-daemon --no-configuration-cache
```

注意：当前配置开启 `org.gradle.configuration-cache=true`，但 Gradle 9.5.1 与现有 `processResources` 逻辑存在兼容问题，实际执行时继续使用 `--no-configuration-cache`。

## 3. 模块拓扑

```text
effi-rpc-bom
effi-rpc-common
  ^
effi-rpc-annotation
  ^
effi-rpc-processor

effi-rpc-common
  ^
effi-rpc-annotation
  ^
effi-rpc-component
  ^
effi-rpc-context
  ^
effi-rpc-registry-api
  ^
effi-rpc-governance

effi-rpc-context
  ^
effi-rpc-metrics

effi-rpc-component
  ^
effi-rpc-marshalling
  ^
effi-rpc-transport-api
  ^
effi-rpc-transport-netty

effi-rpc-transport-api + effi-rpc-governance + effi-rpc-proxy
  ^
effi-rpc-boot
  ^
effi-rpc-protocols:effi-rpc-http
  ^
effi-rpc-protocols:effi-rpc-grpc
```

简化依赖链：

```text
common
  -> annotation
    -> processor
    -> component
      -> context
        -> metrics
        -> registry-api
          -> governance
          -> registry-consul
          -> registry-nacos
        -> boot
          -> http
            -> grpc

component
  -> marshalling
    -> transport-api
      -> transport-netty

component
  -> proxy

boot
  -> transport-api
  -> governance
  -> proxy
```

## 4. 核心心智模型

### 4.1 三级作用域

由 `effi-rpc-component` 提供：

- `ScopedPlatform`：平台级资源，顶层容器。
- `ScopedApplication`：应用级资源，包含多个 module。
- `ScopedModule`：模块级资源，面向一组 caller/servant 配置。

每级都有：

- `ComponentRepository`：普通组件注册与查找。
- `ExtensionRepository`：SPI/扩展实现注册与查找。
- `HierarchicalOptions`：可向父级回溯的配置。
- lifecycle hooks：initialize、start、close。

重要规则：

- 组件注册必须满足作用域匹配。
- 查找只能向父级回溯，不能反向向下查找。
- 默认实例通过 `LazySingleton` 提前暴露，避免递归初始化。

### 4.2 Caller / Servant

由 `effi-rpc-context` 提供：

- `Caller<R>`：调用方，发起异步调用，维护 locator、interceptor chain、reply chain、client config。
- `Servant`：服务方，绑定方法、参数和调用逻辑。
- `CallerGroup` / `ServantGroup`：同一类型下 peer 集合。
- `PeerDescriptor`：调用方/服务方的类型、选项、协议和元信息描述。

### 4.3 调用链

单次调用核心链路：

```text
proxy / caller proxy
  -> Caller.call(...)
    -> CallExecution
      -> caller.callStageChain()
        -> Stage / Interceptor
          -> Locator
            -> registry discovery / router / load balancer
              -> Protocol.createRequest()
                -> Transport send
                  -> reply Future
                    -> reply stage / interceptor
                      -> Promise complete
```

关键类：

- `effi-rpc-context/.../support/CallExecution.java`
- `effi-rpc-context/.../support/AbstractCaller.java`
- `effi-rpc-context/.../support/AbstractServant.java`
- `effi-rpc-context/.../Stage.java`
- `effi-rpc-context/.../Interceptor.java`
- `effi-rpc-context/.../Protocol.java`

### 4.4 传输与协议

`transport-api` 定义：

- Endpoint：Server / Client / Channel。
- Message：InputMessage / OutputMessage。
- Codec：Encoder / Decoder / ClientExchangeContextCodec / ServerExchangeContextCodec。
- TransportProtocol / ProtocolStack。
- InvocationResolver / ClientResponseHandler / ServerRequestHandler。

`transport-netty` 提供：

- NettyChannel / NettyServer / NettyClient / NettyPoolClient。
- Netty channel configurer、消息聚合、空闲检测、SSL 上下文。

协议层：

- `effi-rpc-http`：HTTP/1、HTTP/2、协商、header/path/query/body 参数绑定、HTTP codec。
- `effi-rpc-grpc`：gRPC streaming 和 HTTP/2 之上的调用骨架。

## 5. 模块逐个精读

### 5.1 `effi-rpc-common`

最底层公共能力，禁止依赖上层协议或业务模块。

子包：

- `logging`：Logger 门面、适配器、工厂、SLF4J/Log4j2/JCL/JDK/NoOp。
- `concurrent`：Future、Promise、Futures、Deadline、Result、Flow 消息接口。
- `compile`：运行时动态 accessor、ASM 生成、MethodHandle/反射 fallback、RuntimeClassLoader。
- `config`：SmartURL、QueryPath、URLUtil、IdentifiableConfig。
- `constant`：默认线程数、资源路径、系统键、KeyConstant、Tags。
- `exception`：ErrorCode、EffiRpcException、错误码分配器和预定义错误码。
- `executor`：RpcThreadPool、ConfigurableThreadFactory。
- `hook`：InitializeHook、StartHook、CloseHook、HookExecutor。
- `nativetools`：GraalVM native-image 配置模型和 JSON writer。
- `option`：Options、HierarchicalOptions、OptionName、OptionType、内置类型支持。
- `trait`：Builder、Fluent、Closeable、Identifiable、Ordered、Replicable 等基础 trait。
- `util`：集合、反射、字符串、属性、类型捕获、懒初始化等通用工具。

重点：

- `concurrent` 是框架异步生命周期的基础，Promise 单次赋值、取消和回调注册顺序非常关键。
- `compile` 的 `DynamicAccessor` 被 servant/method binding 调用，影响调用性能。
- `option` 的父子层级解析影响 caller/servant 所有默认配置继承。

### 5.2 `effi-rpc-annotation`

只定义注解，不执行运行时逻辑。

- `@CallGroup` / `@Call`：调用方声明、治理覆盖、超时、重试、协议、序列化等。
- `@ServeGroup` / `@Serve`：服务方声明、限流/协议/序列化等。
- `@Extensible`：声明可扩展 SPI 接口及作用域。
- `@Extension`：声明扩展实现、名称、别名、标签、顺序、scope、primary、override。
- `@ScopedComponent`：声明组件作用域。

### 5.3 `effi-rpc-processor`

注解处理器，入口是 `EffiRpcAnnotationProcessor`。

核心职责：

- 收集 `@Extensible`、`@Extension`。
- 收集 `@ScopedComponent`。
- 收集 `@ServeGroup` / `@CallGroup`。
- 生成扩展资源、组件资源、native-image reflect/proxy/resource 配置。
- 通过 `ResourceCollector` 分 section 写资源。

关键类：

- `ExtensibleHandler`
- `ExtensionHandler`
- `ScopedComponentHandler`
- `ServeGroupHandler`
- `CallGroupHandler`
- `NativeReflectConfigHandler`
- `ResourceCollector`

### 5.4 `effi-rpc-component`

框架的容器、扩展、生命周期和基础配置中心。

核心子域：

- scoped context：`ScopedPlatform` / `ScopedApplication` / `ScopedModule` / `ScopedContext`。
- component registry：`ComponentRepository` / `ComponentRegistry` / `DelegateComponentRepository`。
- extension registry：`ExtensionRepository` / `ExtensionLoader` / `ExtensionEntry`。
- event：`EventBus`、`MpscEventBus`、`EventLane`、`EventHandler`。
- config：client/server/registry/endpoint/TLS/compression/transport options。
- tools：`Scheduler`、`ThreadPool`。

事件总线约定：

- `CONTROL` lane 用于生命周期和连接控制事件，单 consumer、保序，默认队列满时阻塞。
- `TELEMETRY` lane 用于指标等可观测事件，可配置多个 consumer 分片，默认队列满时丢弃。
- 发布方只判断 `EventLane` 和 `BackpressurePolicy`；handler 解析、异常隔离、批处理和关闭 drain 都在 `MpscEventBus` 内完成。
- `EventOptions` 定义平台级 option：`event.capacity`、`event.batchSize`、`event.idleParkNanos`、`event.publishTimeoutNanos`、`event.daemon`、`event.metricsEnabled`、`event.telemetryConsumers`。

2026-10-01 本机 JMH 参考值，4 producer、no-op handler：

- `MpscEventBus` 单 telemetry lane：约 `14.3M ops/s`。
- `MpscEventBus` 4 telemetry lanes：约 `50.3M ops/s`。
- 原生单 consumer Disruptor ring buffer：约 `4.0M ops/s`。

这组数字只说明当前发布路径和队列模型，不等价于完整 RPC 端到端吞吐。

原则：

- annotation 不携带运行逻辑。
- component 只处理容器和扩展，不直接实现协议调用。
- boot 负责选择默认实现并组装完整应用。

### 5.5 `effi-rpc-context`

RPC 调用语义核心，位于 component 之上、boot 和具体 protocol 之下。
本模块定义协议无关的抽象和默认支撑实现，实际 HTTP/gRPC 行为仍以
`effi-rpc-boot` 与 `effi-rpc-protocols` 的实现为准。

**静态模型**

- `Peer` 是 Caller 和 Servant 的共同父接口，统一提供 protocol、query path、
  reply type、thread pool、call/reply stage chain、call/reply interceptor chain、
  hierarchical options 和 module。
- `PeerDescriptor` 是不可变的 peer 描述，字段为
  `kind / protocol / path / replyType / options`。
- `PeerGroup` 管理同类 peer；`CallerGroup` 额外暴露客户端代理，
  `ServantGroup` 额外暴露服务实现、方法索引和调用入口。
- `Caller` 和 `Servant` 都是 module-scoped；`Protocol` 是 platform-scoped 的
  `PeerFactory + MessageFactory`。
- `Message / Request / Response` 表达协议消息边界：
  Request 决定是否需要回复，Response 暴露成功状态和失败原因。
- `CallContext` 发生在请求发送前或服务端方法调用前；
  `ReplyContext` 发生在收到响应后或服务端发送响应前。

**构建与解析**

`AbstractPeer.Builder.build()` 的固定顺序是：

```text
validate
  -> resolve protocol and descriptor
  -> prepare peer-specific dependencies
  -> resolve thread pool / stage chain / interceptor chain
  -> checkState
  -> newInstance
  -> register peer into module and peer group
```

- `Caller.Builder.prepare()` 解析 `Locator`、`ClientConfig` 和
  `Unary.FailureHandler`。
- `AbstractCaller.call()` 创建 `CallExecution`，统一处理超时、取消、重试和最终完成。
- `AbstractServant.invoke()` 委托给 `ServantGroup.invoke()`；实际方法定位由
  `ServantGroup.indexOf()` 和 `MethodBinder` 完成。
- peer options 注册到 group 后以 group options 为 parent；
  `AbstractPeer` 再把 descriptor options 的 owner 设为自身。

**Invocation 与参数绑定**

- `PositionalInvocation` 表示没有方法签名元数据的直接调用；
  `MethodInvocation` 在它之上增加 `MethodSignature`。
- `InvocationArguments` 是有序参数数组；
  `InvocationAttributes` 存放协议无关或协议私有的附加属性。
- `MethodBinding` 表示一个方法的全部参数绑定，
  `positional=true` 时使用数组位置，否则通过 invocation attributes 绑定。
- `PositionParameterBinder` 只处理位置参数；
  `AnnotationParameterBinder` 通过注解的 `Writer / Reader` 做双向绑定。
- `MethodBinder.bind()` 校验参数数量并生成 `MethodInvocation`；
  `MethodBinder.resolve()` 按 `ParameterBinding` 逐项取值并调用
  `ReflectionUtil.convertToParameterType()` 做参数类型转换。
- `Body / Header / PathVar / ParamVar` 以及 `Argument.Source / Target`
  是参数来源和目标注入的中间模型。

**调用链**

默认调用方链由 boot 的 `DefaultStageChainResolver` 组装：

```text
CallInterceptorStage
  -> LocatorStage
    -> ChosenInterceptorStage
      -> CallAttemptStage
        -> tail
```

实际执行时：

1. `CallExecution.execute()` 启动一次逻辑调用；
2. 创建 `CallContext`，由 `Protocol.createRequest()` 创建协议请求；
3. `CallInterceptorStage` 执行 `callInterceptorChain`；
4. 拦截器链尾部的 `StageInterceptor` 继续执行下一 stage；
5. `LocatorStage` 调用 `Caller.locator().locate(context)` 并写入目标地址；
6. `ChosenInterceptorStage` 执行目标选择后的 `chosenInterceptorChain`；
7. `CallAttemptStage` 创建 `ReplyFuture`、`CallAttempt` 并交给 transport；
8. transport 完成响应后进入 reply stage chain；
9. `CallExecution` 读取 `ReplyFuture.rawResult()`，最终完成自身的 `Promise`。

**服务端链**

服务端由具体 protocol 的 invocation resolver 把协议消息转换为
`PositionalInvocation`，然后执行：

```text
CallInterceptorStage
  -> interceptor chain
    -> InvokeServantStage
      -> Servant.invoke()
        -> ServantGroup.invoke()
```

`InvokeServantStage` 把返回值或异常包装为 `Interaction.Result`；
protocol 再通过 `MessageFactory.createResponse()` 生成协议响应。

**回复链**

服务端和客户端的 reply phase 使用同一套 `ReplyContext` 语义：

```text
ReplyInterceptorStage
  -> reply interceptor chain
    -> ReplyResultStage
      -> Interaction.Result
```

`CallAttemptStage` 注册的完成回调会执行调用方 reply stage chain；
`ReplyResultStage` 是最终结果出口，负责把 `ReplyContext.result()` 继续向上返回。

**失败、超时与取消**

- `Unary.FailureHandler` 是 platform-scoped 扩展点。
- `FailFast` 直接抛出失败；`FailRetry` 只对显式 retryable 标记或
  `SERVICE_UNAVAILABLE / SERVER_OVERLOADED` 重试。
- `CallExecution` 从 `CallerOptions.TIMEOUT` 创建 `Deadline`；
  timeout < 0 表示无 deadline。
- deadline 到期会取消整个 completion，并取消活动 attempt；
  取消也会向当前 `ReplyFuture` 传播。
- 重试延迟使用 `RETRY_BACKOFF + 指数增长 + RETRY_JITTER`，
  最大不超过 `RETRY_MAX_BACKOFF`。

**Future 与在途调用**

- `ReplyFuture` 是协议层和调用语义层之间的 unary future，
  构造时向 platform-scoped `CallFutureRegistry` 注册唯一 id。
- 唯一 id 同时写入 `CallContext` 和 `SmartURL`，用于 transport
  完成响应后找回对应 future。
- `CallFutureRegistry` 在 future 完成时自动移除；
  platform 关闭时取消全部在途 future。
- `ReplyFuture.complete()` 先保存 `rawResult`，再完成 delegate Promise；
  reply stage chain 的结果最终由 `CallExecution` 读取。

**指标**

- `CallerMetrics` 和 `CalleeMetrics` 挂在 peer attributes 上，
  由 future/call 事件处理器更新。
- caller 侧由 `CallExecution` 记录开始时间，
  `CallerMetricsInterceptor` 记录结束时间并发布 `CallerMetricsEvent`。
- callee 侧由 `CallExecuteRecordInterceptor` 记录开始和结束时间。
- `MetricsSupport` 的计时 key 放在 `CallContext` attributes 中。

**当前边界**

- `MetricsEvent` 当前通过 `END_TIME - START_TIME` 计算执行时长，
  代码中又除以 `1_000_000`；这与“duration 为 nanoseconds”的注释不一致，
  维护时要先确认单位约定。
- `MetricsEvent` 直接对计时 key 做 `Long` 拆箱；如果某个协议没有写入
  可选计时点，会增加 NPE 风险。
- `AnnotationStyle` 使用静态缓存和
  `ScopedPlatform.defaultInstance()`；多 platform、同名 style 场景需要
  特别确认缓存隔离是否符合预期。
- `ImmutableStageChain` 和 `ImmutableInterceptorChain` 的 tail 返回
  `null`，这是链结束标记而不是业务结果。自定义链必须保证最后有
  `ReplyResultStage` 或等价的 result-producing tail。
- `context` 只定义契约和默认执行语义；HTTP/gRPC 的 message、codec、
  transport 绑定必须回到对应 protocol 模块核对。

### 5.6 `effi-rpc-boot`

默认启动、默认实现和注解驱动的组装层。

核心职责：

- `EffiRpcBootstrap`：应用启动入口。
- `ApplicationServiceRegistrar`：注册/注销/启动/关闭协调器。
- `DefaultLifecycleConfiguration`：默认 platform/application listener。
- `AnnotationCallerGroup` / `AnnotationServantGroup`。
- `InterfaceCallerGroup` / `InterfaceServantGroup`。
- 默认 stage chain、interceptor chain、thread pool resolver。
- `ServerLauncher`：服务端绑定和关闭。
- `DefaultThreadPoolResolver`：按 kind 创建/复用 caller/servant 线程池。

启动链：

```text
EffiRpcBootstrap.start()
  -> ScopedApplication.start()
    -> ScopedModule.start()
      -> ApplicationServiceRegistrar.register()
        -> bind servers
          -> register to registries
            -> mark READY
```

关闭链：

```text
EffiRpcBootstrap.stop()
  -> ScopedApplication.close()
    -> ApplicationServiceRegistrar.deregister()
      -> deregister services
        -> close servers
          -> release modules/platform resources
```

### 5.7 `effi-rpc-governance`

服务治理层。

核心职责：

- `ServiceDiscovery`：服务发现抽象。
- `Router` / `DefaultRouter`：根据路由配置选目标。
- `LoadBalancer` / `RandomLoadBalancer` / `RoundRobinLoadBalancer`。
- `RegistryLocator`：组合 discovery、router、load balancer 完成实际定位。
- `ServiceRegistrar`：注册/注销协调。
- `RegistryLocatorLifecycle`：平台生命周期注册。

关键调用路径：

```text
Locator.locate(context)
  -> RegistryLocator
    -> ServiceDiscovery.lookup()
      -> Router.filter()
        -> LoadBalancer.select()
```

### 5.8 `effi-rpc-proxy`

动态代理层。

核心职责：

- `ProxyFactory`：创建接口代理或对象代理。
- `AbstractProxyFactory`：通用代理工厂流程。
- `JDKProxyFactory` / `JDKInvocationHandler`。
- `CGLibProxyFactory` / `CGLibMethodInterceptor`。
- `ByteBuddyProxyFactory`。
- `MethodInterceptor` / `InvocationHandler`：统一代理回调模型。

用途：

- `consume(Class<T>)` 返回接口代理。
- 代理调用进入 Caller 调用链。

### 5.9 `effi-rpc-marshalling`

序列化和压缩能力。

核心职责：

- `Serializer`：序列化/反序列化 SPI。
- `AbstractSerializer`：公共模板。
- `JdkSerializer`、`JacksonSerializer`、`KryoSerializer`、`ProtobufSerializer`。
- `Compressor`：GZIP、Deflate、LZ4、Snappy。
- `CompressibleSerializer`：序列化和压缩组合。

依赖特点：

- Jackson、Kryo、Protobuf、LZ4、Snappy 均为 compileOnly 或可选依赖。

### 5.10 `effi-rpc-metrics`

当前模块只有 Gradle 配置，依赖 `effi-rpc-context`，没有实际 Java 源码。

结论：

- 指标能力目前主要表现为 context 中的 metrics 抽象和事件，不是独立 metrics 实现模块。
- 后续恢复记忆时不要把该模块误认为已有独立采集/上报实现。

### 5.11 `effi-rpc-transport:effi-rpc-transport-api`

传输抽象层。

核心职责：

- Endpoint：Server / Client / Channel / Endpoint。
- Message：InputMessage / OutputMessage / IOMessage。
- Codec：Encoder / Decoder / ConfigurableCodec。
- Exchange：ServerExchange / ClientResponseHandler / ServerRequestHandler。
- ChannelCallBindings：请求 ID 与 future 绑定/取消。
- Idle detection：IdleEvent / RefreshIdleCountEvent。
- TransportProtocol / ProtocolStack / Transporter。

### 5.12 `effi-rpc-transport:effi-rpc-transport-netty`

Netty 传输实现。

核心职责：

- `NettyServer` / `NettyClient` / `NettyPoolClient`。
- `NettyChannel`：Netty Channel 包装，承担 promise 取消向 ChannelFuture 传播。
- `NettyEndpoint` / `NettySupport`。
- `EndpointChannelConfigurer` / `ChannelConfigurer`。
- `ClientMessageAggregator` / `ServerMessageAggregator`。
- `IdleDetectionHandler`。
- `SslContextManager`。

### 5.13 `effi-rpc-protocols:effi-rpc-http`

HTTP 协议实现。

核心职责：

- HTTP/1：`Http1Protocol`、`Http1Server`、`Http1Client`、codec/handler。
- HTTP/2：`Http2Protocol`、`Http2Server`、`Http2Client`、stream 模型。
- 协议协商：`HttpNegotiationHandler`、`HttpClearTextSniffHandler`。
- 参数绑定：body/header/path/query binder。
- JAX-RS 风格：`JaxRsStyleResolver`、`HttpServantMethodBuilder`。
- HTTP 消息：`HttpRequest`、`HttpResponse`、`HttpHeaders`、`HttpIOMessage`。

### 5.14 `effi-rpc-protocols:effi-rpc-grpc`

gRPC 协议骨架。

现有能力：

- `GrpcCaller`
- `StreamObserver`
- `ServerStreamFuture`

注意：

- 当前 streaming 相关实现部分仍是占位或未完整实现。
- 该模块应视为 HTTP/2 + gRPC 方向的未完成区域，不能当成成熟 gRPC 实现。

### 5.15 `effi-rpc-registry:effi-rpc-registry-api`

注册中心抽象。

核心职责：

- `RegistryClient`：register / deregister / lookup。
- `AbstractRegistryClient`：注销、订阅、心跳、重试、关闭的统一流程。
- `RegisterTask`：单实例注册重试任务。
- `DefaultServiceInstance` / `ServiceInstance`。
- `RegistrationPreparer`、`RegistryUtil`。
- `AbstractRegistryClientFactory`。

### 5.16 `effi-rpc-registry:effi-rpc-registry-consul`

Consul 注册中心实现。

- `ConsulRegistryClient`
- `ConsulRegistryClientFactory`
- `ConsulErrorCodes`
- `VertxCloser`

### 5.17 `effi-rpc-registry:effi-rpc-registry-nacos`

Nacos 注册中心实现。

- `NacosRegistryClient`
- `NacosRegistryClientFactory`
- `NacosOptions`
- `NacosErrorCodes`

### 5.18 `effi-rpc-test`

测试模块，覆盖核心行为：

- Scoped platform/lifecycle。
- Extension repository 和递归扩展。
- CallExecution、CallFutureRegistry、MethodBinding。
- FailRetry。
- Registry locator、router。
- Option strategy/types。
- HTTP protocol、HTTP/2 stream。
- Netty support、endpoint configurer、channel bindings。
- LoggerFactory、Promise、DynamicAccessor。
- Spring Boot 集成测试（仅作为测试依赖存在）。

### 5.19 `effi-rpc-demo`

演示模块，帮助快速恢复典型使用方式：

- `demo/api`：共享接口。
- `demo/provider`：提供方。
- `demo/consumer`：调用方。
- 同时包含接口式、注解式和 Spring Boot demo 入口。

## 6. 扩展机制精要

### 6.1 `@Extension` 加载流程

```text
注解处理器扫描 @Extension
  -> 写 META-INF/effi-rpc/services/...
    -> 运行时 ExtensionRepository
      -> ExtensionLoader
        -> ExtensionEntry
          -> scoped context 查找已有实例
          -> 找不到则反射创建
          -> 注入 ScopedPlatform/Application/Module
          -> 通知 ExtensionLoadedListener
```

### 6.2 扩展选择顺序

主要因素：

- `name`
- `tag`
- `order`
- `primary`
- `override`
- `scope`
- `onClass` 条件

### 6.3 组件作用域

组件不允许跨层注册。查找时只允许通过父上下文向上回溯。

## 7. 线程、异步与生命周期

### 7.1 线程池

- `ConfigurableThreadFactory` 是统一线程工厂。
- `RpcThreadPool` 是底层 `ThreadPoolExecutor` 创建器。
- `component/tools/ThreadPool` 是组件级包装，提供 Future、metrics、close。
- `component/tools/Scheduler` 提供 disposable/periodic 调度。

### 7.2 异步模型

- 框架统一使用 `Future` / `Promise` / `Result`。
- `Promise` 是单次赋值。
- `CallExecution` 负责超时、取消、重试、回调。
- `ReplyFuture` 负责一次远程交互结果。
- `CallFutureRegistry` 负责在途请求绑定和取消。

### 7.3 生命周期

```text
initialize
  -> start
    -> active
      -> close
```

任一级作用域关闭时：

- 传播到子级。
- 释放组件和扩展。
- 关闭线程池和 scheduler。

## 8. Native Image

相关能力：

- `nativetools` 定义 Reflect / Proxy / Resource / Serialization / JNI 配置。
- 注解处理器生成 native-image reflect/proxy/resource 配置。
- `NativeUtil.inNativeImage()` 判断当前是否为 native image。
- `DynamicAccessorFactory` 在 native image 下优先走 fallback accessor。

## 9. 不能轻易破坏的不变量

- `Caller` / `Servant` / `Protocol` 的 scoped component 作用域。
- 组件只能向上查找，不能向下查找。
- `Promise` 的单次赋值语义。
- `CallExecution` 的 deadline / cancel / retry 顺序。
- `ReplyFuture` 与 `CallFutureRegistry` 的 future 绑定键。
- `Protocol` 的 request/reply 创建契约。
- `TransportProtocol` / codec 的 client/server 对称性。
- `RegistryClient` 的 register、deregister、lookup 收敛语义。
- 注解处理器生成的资源路径和内容格式。

## 10. 当前边界与风险

- Spring Boot starter 是集成边界，本文不展开。
- `effi-rpc-grpc` streaming 能力尚未达到完整生产实现。
- `effi-rpc-metrics` 当前没有独立源码实现。
- HTTP/3、Triple 支持尚未完成。
- `PRODUCTION_READINESS.md` 记录的是更严格的生产加固清单，不能把“代码可启动”误认为“生产已完备”。
- 选择扩展、注册中心、协议、线程池时必须同时检查 compileOnly 可选依赖是否存在于运行时。

## 11. 快速恢复记忆检查表

1. 先看 `settings.gradle.kts` 确认模块拓扑。
2. 看 `build.gradle.kts` 和各模块 build 文件确认依赖方向。
3. 从 `EffiRpcBootstrap.start()` 进入启动链。
4. 从 `Caller.call()` 和 `CallExecution` 进入调用链。
5. 从 `Servant` / `ServerRequestHandler` 进入服务端执行链。
6. 从 `ExtensionRepository` / `ExtensionEntry` 理解扩展加载。
7. 从 `RegistryLocator` 理解服务发现、路由和负载均衡。
8. 从 `TransportProtocol` / `AbstractProtocol` 进入协议与传输。
9. 从 `RegistryClient` / `AbstractRegistryClient` 进入注册中心。
10. 从 `PRODUCTION_READINESS.md` 查看还未完成的生产加固项。

