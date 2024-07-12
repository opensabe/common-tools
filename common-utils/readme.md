# common-utils 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>common-utils</artifactId>
<version>去 maven 看最新版本</version>
```
## 工具类

### 内存队列合并事务

为啥要合并事务参考：
- https://aws.amazon.com/cn/blogs/database/analyze-amazon-aurora-mysql-workloads-with-performance-insights/
- https://www.modb.pro/db/70174

#### 队列抽象与使用场景

##### 1. BatchBufferedQueue

适用场景：
1. 本来的更新，外部没有包裹事务，将这些更新合并为一个大事务，可以是**异步更新**，**更新可以丢失**（业务上有对应的补偿），并且合并的这些更新中基本不会有异常，有异常导致整体回滚也是可以接受的。

使用规范：

可以配置的参数与考虑

##### 2. BatchBufferedCountDownQueue

适用场景：
1. 本来的更新，外部没有包裹事务，将整个事务方法封装进入回调，与其他事务合并成为一个大事务，但是需要是同步更新，并且更新有异常，同步调用抛出这个异常。更新内部分为两种情况：
1. 更新内部一般不会有异常，如果有，回滚合并的整个事务也是可以接受的，这样所有对于 submit 的同步调用都会抛出同一个异常
2. 更新的内部通过业务异常控制事务回滚，不能因为某个事务的异常回滚，导致整个合并事务回滚，这种使用的时候需要使用 NESTED 进行控制。这样是有发生异常的对于 submit 的同步调用，才会抛出异常，其他的成功。

使用规范：

##### 3. BatchBufferedCountDownWithResultQueue

在 BatchBufferedCountDownQueue 的基础上，每个对于 submit 的同步调用需要返回一个结果。

使用规范：