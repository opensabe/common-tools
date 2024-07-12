# SpringUtil

如果之后再用 SpringUtil 这种工具，遇到 ApplicationContext 为空。那么估计是将这个 SpringUtils 放在 Bean 初始化里面用了。
那么让对应的 Bean 实现 ApplicationListener<ApplicationStartedEvent>, 或者实现 ApplicationRunner，将 SpringUtils 逻辑放在里面执行就行。
一些定时开始，或者 Bean 就绪之后立刻开始调用的，例如 RocketMQ 消费，可能这时候还没有初始化好所有的 Bean，那么可以阻塞直到收到 ApplicationListener<ApplicationReadyEvent>


Spring boot 中，事件主要包括：
- `ApplicationStartingEvent`：这个是spring boot应用一开始启动时，发出的事件，只是用来标识，应用开始启动了，一般没什么用
- `ApplicationEnvironmentPreparedEvent`：这个是在创建好Environment（通过上下文配置，判断到底创建StandardServletEnvironment（针对Servlet环境），StandardReactiveWebEnvironment（针对Reactive环境）还是StandardEnvironment（针对无servlet环境））之后发出的事件。
- `ApplicationContextInitializedEvent`: 这个是在创建好Context并调用ApplicationContextInitializer初始化context之后发布这个事件，在加载bean信息之前
- `ApplicationPreparedEvent`:加载bean信息之后，但是还没有创建bean的时候，发步这个事件。这个事件是和调用`ApplicationContextAware`设置ApplicationContext一起进行的，可以看出，setApplicationContext方法里面不能去获取bean，因为bean可能还没有初始化完成
- `ApplicationStartedEvent`: 加载初始化各种需要的bean并依赖注入之后，在运行`ApplicationRunner`做一些用户自定义的初始化操作之前，会发布这个事件。
- `ApplicationReadyEvent`：运行`ApplicationRunner`做一些用户自定义的初始化操作之后，会发布这个事件。

# Secret 相关

所有依赖会在对应的依赖拦截将一些 secret 不小心打印出来或者保存到某些存储的问题。只需要在对应的依赖模块中，或者微服务中增加 [SecretProvider.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fopensabe%2Fcommon%2Fsecret%2FSecretProvider.java) 的继承，并且注册到 ApplicationContext 作为一个 Bean 即可。