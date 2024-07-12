# spring-boot-starter-s3 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-s3</artifactId>
<version>去 maven 看最新版本</version>
```

**增加配置**：
```
aws:
  s3:
    # accesskey id
    accessKeyId: ${accessKeyId}
    # accesskey key
    accessKey: ${accessKey}
    # 默认的桶，如果使用的是 S3ClientWrapper，则使用的就是这个桶，FileService 可以指定桶也可以不指定，不指定使用的就是这个默认桶
    defaultBucket: ${defaultBucket}
    # 仅对 S3ClientWrapper 生效，文件都会上传到这个文件夹，下载文件也会从这个文件夹中寻找，必须包含至少一个二级目录，例如 online/public
    folderName: ${folderName}
    # s3 位于的区域
    region: ${region}
    # endpoint 是代理地址，我们只有在本地启用的时候，才会需要配置代理翻墙，v2ray 默认配置 http://127.0.0.1:1081 即可
    endpoint: http://127.0.0.1:1081
    # 仅对 S3AsyncTaskFileService 有效
    profile:
    # 仅对 S3AsyncTaskFileService 有效
    staticDomain:
```
对于从 common-s3wrapper 改用 spring-boot-starter-s3 的项目，建议使用 `S3ClientWrapper` 替换原来的 `S3Client`，方法都一样。并且**配置也不用修改**

## S3 与反向代理

一般会针对 s3 做反向代理，例如：

静态资源类别 | 环境     | S3桶名称 |S3 文件夹名称 |访问路径
---|--------|---|---|---|---|---
上传的供前端访问的静态资源 | test   | test | test/xxxx | https://test1.xxxxx.com/static
上传的供前端访问的静态资源 | online | test | online/xxxx | https://www.xxxxx.com/static

例如文件夹，online/xxxx/x-service，对应的映射地址就是： https://www.xxxxx.com/static/x-service

## 使用 S3ClientWrapper
```
@Autowired
private S3ClientWrapper s3ClientWrapper;
```
**1. 上传文件**
```
byte []bytes = inputStream.readAllBytes();
String filename = s3ClientWrapper.upload(bytes);
//这样，就上传到了配置中指定桶的文件夹下，文件名通过文件的 md5 生成
```

```
byte []bytes = inputStream.readAllBytes();
s3ClientWrapper.uploadWithOriginName(bytes, "funny-cat.jpeg", "image/jpeg", null);
//这样，就上传到了配置中指定桶的文件夹下，文件名为 funny-cat.jpeg， 文件类型为 image/jpeg
```
**2. 下载文件**
```
InputStream download = s3ClientWrapper.download("funny-cat.jpeg");
byte[] allBytes = download.readAllBytes();
```
**3. 验证文件是否存在**
```
boolean doesObjectExists = s3ClientWrapper.doesObjectExists(fileName);
```
**4. 复制文件**
```
s3ClientWrapper.copy("funny-cat.jpeg", "funny-cat-copy.jpeg");
```
## 使用 FileService（待补充）

### s3操作监控

事件名称：s3.operate
触发时机：执行S3Client的putObject,getObject等方法时

#### HighCardinalityKeyValues

| 属性  |类型| 备注  |
| ------------ | ------------ |
|s3.file.name| string | 文件名 |
| s3.file.operate_successfully    | boolean  | s3操作是否成功 |
| s3.file.operate_type  |string|操作类型,如putObject|
| s3.file.size | int | 文件大小 |

#### LowCardinalityKeyValues

| 属性  |类型| 备注  |
| ------------ | ------------ |
| s3.file.operate_successfully    | boolean  | s3操作是否成功 |
| s3.file.operate_type  |string|操作类型,如putObject|
| s3.file.size | int | 文件大小 |

#### JFR

事件名称：operation
事件分类（所属文件夹）：observation.s3

|属性|备注|
| ------------ | ------------ |
|file name|文件名|
|file size|文件大小|
|operate type|s3操作类型,如putObject|
|traceId||
|spanId||
| success | 是否获取成功 |
