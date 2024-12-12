# spring-boot-starter-location 使用文档

## 使用说明

**引入依赖**：
```
<groupId>software.amazon.awssdk</groupId>
<artifactId>geoplaces</artifactId>
<version>去 maven 看最新版本</version>
```

**增加配置**：
```
aws:
  location:
    # accesskey 
    accessKey: ${accessKey}
    # secretKey
    secretKey: ${secretKey}
    # location 位于的区域
    region: ${region}
    # true为开启location功能 false为关闭
    enabled: true
```

## 使用 GeocodeService
```
@Autowired
private GeocodeService geocodeService;
```
**1. 根据text查询经纬度**
```
 private String address = "Samuel Asabia House 35 Marina,Lagos,Nigeria";
 List<Double> coordinates = geocodeService.getCoordinates(address);
//这样，就可以获取address对应的经纬度了
```




