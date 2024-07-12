# spring-cloud-node-manager 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-cloud-node-manager</artifactId>
<version>去 maven 看最新版本</version>
```

**增加配置**：node manager 依赖 redis，需要项目配置 redis 相关配置。

**获取全局唯一 id**：

spring-cloud-node-manager是用来为同一个微服务下每一个实例提供一个唯一的，可复用的nodeId，nodeId是一个整数，而且是递增的。

例如微服务有两个实例A，B，A的nodeId为0，B的nodeId为1；这时候新增实例C，C的nodeId就是2；如果B挂了，D启动了，那么D的nodeId就是1；

仅适用于应用启动时获取一个nodeId的场景，实现原理：

```
获取redis锁：
	访问集群中每一个同zone的实例，获取每个nodeId
	从0开始，遍历数字，看那个数字还没用，就用这个作为nodeId。
释放redis锁
```
这样做，同时启动，关闭多个实例，都不会重复。但是有一种情况下可能会重复，就是在一个实例启动时，目前在线的一个实例突然不能访问了，过一会又恢复了，这样可能这个实例的nodeId被新启动的实例占用，不过在我们的应用场景下，这个出现概率很低。

使用方法：

```
@Autowired
private NodeManager nodeManager;


//获取nodeId，使用
int node = nodeManager.getNodeId();
```