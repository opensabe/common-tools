# spritest-boot-starter-mybatis 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spritest-boot-starter-mybatis</artifactId>
<version>去 maven 看最新版本</version>
```


**增加配置**：

以下配置一般每个国家一样，所以建议放入 bootstrap.yml
```
################ mybatis配置 ################
mapper:
  mappers:
    # 指定公共加载的 mappers，这里需要加载 BaseMapper，这样可以使用 BaseMapper 的特性
    - io.github.opensabe.common.mybatis.base.BaseMapper
mybatis:
  configuration:
    # 对于下划线命名的数据库实体映射为驼峰命名的实体，例如字段 create_time 映射为 createTime
    map-underscore-to-camel-case: true
pagehelper:
  # pagehelper 属性，请参考：https://pagehelper.github.io/docs/howtouse/
  offset-as-page-num: true
  support-methods-arguments: true
```
配置数据库：
```
# 配置operId与国家的映射，这个决定了使用的数据源是哪个集群的，目前2代表ng
country:
  map:
    2: ng
# 数据源默认的 OperId 是，这个决定了使用的数据源是哪个集群的，目前2代表ng，填写2默认会使用cluster-name为ng的数据源
defaultOperId: 2
jdbc:
  config:
    ################ 自己的业务库库 ################
    main:
      base-packages:
        - io.github.opensabe.factsCenter.common.dal.db.dao
        - io.github.opensabe.factsCenter.common.revo.dal.db.mapper
      data-source:
        # 写数据库（主）
        -   cluster-name: ng
            driver-class-name: com.mysql.jdbc.Driver
            is-write-allowed: true
            slow-sql-millis: 3500
			# 初始连接个数，本地或者测试环境最好是 1，防止建立过多连接过慢
			initial-size: 1
            name: test-main-1
            password: 'XDBPASSWDX'
            url: jdbc:mysql://test-aurorawr.s.opensabe.:3306/opensabe_facts?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&netTimeoutForStreamingResults=0&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC
            username: hopegame
        # 读数据库（从）
        -   cluster-name: ng
            driver-class-name: com.mysql.jdbc.Driver
            slow-sql-millis: 3500
			# 初始连接个数，本地或者测试环境最好是 1，防止建立过多连接过慢
			initial-size: 1
            # 从库是false
            is-write-allowed: false
            name: test-main-2
            password: 'XDBPASSWDX'
            url: jdbc:mysql://test-auroraro.s.opensabe.:3306/opensabe_facts?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&netTimeoutForStreamingResults=0&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC
            username: hopegame
      default-cluster-name: ng
      transaction-service-packages:
        - io.github.opensabe.factsCenter
```


## 使用公共 Mapper

定义表实体，注意在上面的配置中我们配置了**对于下划线命名的数据库实体映射为驼峰命名的实体**，所以数据库字段为下划线名称，对应实体是驼峰。例如下表：
```
@Data
@NoArgsConstructor
//填写真正的表名
@Table(name = "t_realsports_selection")
public class RevoSelectionPO {
    //如果使要用selectById方法，需要指定主键字段
    @Id
    private String id;
    //index是保留字，所以需要改名被`包裹
    @Column(name = "`index`")
    private String index;
    //对应 jointed_id 字段
    private String jointedId;
    //对应 event_start_time 字段
    private Timestamp eventStartTime;
    //...省略其他属性
}
```
使用公共 mapper 定义Mapper,注意目录要在数据库配置的对应数据源扫描mapper的目录下：
```
public interface RevoSelectionMapper extends BaseMapper<RevoSelectionPO> {
    //如果有复杂的 group by 以及  join 的语句，不能通过公共 mapper 进行描述，需要使用基于注解的 SQL，如下面这个例子：
    @Results(
            id = "selectionResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "index", column = "`index`"),
            @Result(property = "jointedId", column = "jointed_id"),
            @Result(property = "eventStartTime", column = "event_start_time")
    })
    @Select({
            "<script>",
            "select distinct selection.*, subbet.bet_id betId from ",
            "<if test=\"isHistory\">",
            "t_realsports_subbet_his",
            "</if>",
            "<if test=\"!isHistory\">",
            "t_realsports_subbet",
            "</if>", "subbet",
            "join",
            "<if test=\"isHistory\">",
            "t_realsports_subbet_selection_his",
            "</if>",
            "<if test=\"!isHistory\">",
            "t_realsports_subbet_selection",
            "</if>", "subbet_selection",
            "on subbet.id = subbet_selection.subBet_id",
            "and subbet.user_id = subbet_selection.user_id",
            "join",
            "<if test=\"isHistory\">",
            "t_realsports_selection_his",
            "</if>",
            "<if test=\"!isHistory\">",
            "t_realsports_selection",
            "</if>", "selection",
            "on selection.id = subbet_selection.selection_id",
            "and selection.user_id = selection.user_id",
            "where bet_id in (",
            "<foreach item='item' index='index' collection='betIds' open='' separator=',' close=''>",
            "#{item}",
            "</foreach>",
            ")",
            "<if test=\"userId != null and !userId.isBlank()\">",
            "and selection.user_id = #{userId}",
            "</if>",
            "</script>",
    })
    List<RevoSelectionPO> selectByBetIds(@Param("betIds") Collection<String> betIds, @Nullable @Param("userId") String userId, @Param("isHistory") boolean isHistory);

}
```
我们一般不直接使用 Mapper，而是在其上面封装 Service，通过 BaseService：
```
//注意这里需要注册为 Bean
@Component
public class RevoSelectionMapperService extends BaseService<RevoSelectionPO> {
    @Autowired
    private RevoSelectionMapper revoSelectionMapper;
    
    public Set<String> getUserBettingMatches(String userId, Timestamp start, Timestamp end) {
        Weekend<RevoSelectionPO> weekend = Weekend.of(RevoSelectionPO.class);
        weekend.setDistinct(true);
        //不设置的话就是获取所有字段，这里限制只获取 event_id
        weekend.selectProperties(revoSelectionMapperService.getFieldForWeekend(RevoSelectionPO::getEventId));
        WeekendCriteria<RevoSelectionPO, Object> weekendCriteria = weekend.weekendCriteria();
        weekendCriteria.andEqualTo(RevoSelectionPO::getUserId, userId);
        //真实体育
        weekendCriteria.andIn(RevoSelectionPO::getProduct, RevoSelectionPO.REAL_SPORT);
        weekendCriteria.andGreaterThanOrEqualTo(RevoSelectionPO::getEventStartTime, start);
        weekendCriteria.andLessThanOrEqualTo(RevoSelectionPO::getEventStartTime, end);
        //由于继承了 BaseService 可以直接调用其方法
        //相当于 select distict event_id from t_realsports_selection where user_id = ? and prduct in (?) and event_start_time >= ? and event_start_time <= ?
        return selectByExample(weekend)
            .stream().map(RevoSelectionPO::getEventId).collect(Collectors.toSet());
    }
}
```
其他 BaseMapper 用法，请参考： BaseService 的源码，可以将同一个表的一些简单事务放在这一层

**分页查询或者限制数量**：
```
//使用 pageHelper，需要页码和总数等额外信息
//这是通过执行语句前 count 实现的，如果没必要分页只限制 limit 就不要这么用
PageHelper.startPage(0, 100);
Page<RevoSelectionPO> result = (Page<RevoSelectionPO>) selectByExample(weekend);

//使用 pageHelper，不需要需要页码和总数等额外信息
//增加 false 参数，避免执行语句前有 count
PageHelper.startPage(0, 100, false);
List<RevoSelectionPO> result = selectByExample(weekend);
//或者只是为了limit 100
List<RevoSelectionPO> result = selectLimitByExample(weekend, 100);
```

## 事务有效性检查报警
有些情况下，事务可能失效，例如配置不当（例如哪些包下的 @Transactional 注解使用该数据源的事务管理器，有些包没被配置到所以没有事务管理器），所以增加了这个切面报警，如果事务没生效，则会有 FATAL 日志

但是针对开发规范中的非 bean 调用也是检测不到的，因为非 bean 调用走不到切面，这个需要着重注意
## 历史表

某些表业务不用存储所有数据，会把一些数据归档到历史表。

但有些时候我们需要用这些历史表，同时考虑到这些历史表其实和源表是同构的，SQL也可以复用，就是改一下表名，所以可以通过如下方式进行切换。

继承 `ArchivedTable` 类，实现接口：
```
@Table(name = RevoSelectionPO.TABLE_NAME)
public class RevoSelectionPO extends ArchivedTable {
    public static final String TABLE_NAME = "t_realsports_selection";
    public static final String HISTORY_TABLE_NAME = "t_realsports_selection_his";

    @Override
    protected String tableName() {
        return TABLE_NAME;
    }

    @Override
    protected String historyTableName() {
        return HISTORY_TABLE_NAME;
    }
}
```
使用的时候，如果需要切换到历史表，则:
```
//一次性的
ArchivedTable.setIsHistory(true);
执行MyBatis Mapper
```
或者可以：
```
        WeekendCriteria<RevoSelectionPO, Object> revoSelectionPOObjectWeekendCriteria = revoSelectionPOWeekend.weekendCriteria();
        revoSelectionPOObjectWeekendCriteria.andEqualTo(RevoSelectionPO::getUserId, userId);
        revoSelectionPOObjectWeekendCriteria.andEqualTo(RevoSelectionPO::getId, rollbackPO.getSelectionId());
        //动态指定表名称
        revoSelectionPOWeekend.setTableName(isHistory ? RevoSelectionPO.HISTORY_TABLE_NAME : RevoSelectionPO.TABLE_NAME);
        RevoSelectionPO selection = selectionMapperService.selectOneByExample(revoSelectionPOWeekend);
```

## 切换只读数据库

某些语句我们想只发送到从库上面运行，有两种方式：

**通过 SQL 语句增加注释`/*# mode=readonly */`**：
```
@Select({"<script>select /*# mode=readonly */ odds from (select odds,product,create_time from t_risk_selection where event_id = #{eventId} and market_id = #{marketId} <if test = 'specifier != null'> and specifier = #{specifier} </if> and product in (3) and status = 1 order by create_time desc) temp limit 10000</script>"})
List<Long> getPrematchWinningLatestOdds(@Param("eventId") String eventId, @Param("marketId") int marketId, @Param("specifier") String specifier);
```

**通过公共 Mapper 特定 api(带ReadOnly的方法)**
```
orderRecordMapperService.selectByExampleReadOnly(weekend);
```



## MyBatis自定义AESTypeHandler

	有时候我们需要对数据库里的某些字段做同一处理(例如：加密，解密，枚举值跟名称转换等)，可以自定义typeHandler
	我们公用MyBatis框架对自定义TypeHandler支持比较完善。

### TypeHandler启用方法

#### 通用查询

##### entity代码
```
/**
 * 通用查询指的是用BaseService或者BaseMapper封装的方法查询
 * 由于通用查询都是通过实体类来查，因此需要在实体类的字段上标明该字段使用哪个TypeHandler
 */
 @Getter
 @Setter
 @Table(name = "t_patron_user")
 public class User {
	
	@Id
	private String id;
	
	@ColumnType(jdbcType=JdbcType.VARCHAR, typeHandler = AESTypeHandler.class)
 	private String password;
 }
```
##### AESTypeHandler代码
```
如果大家需要其他的加密算法，也可以参照这个类，继承CryptTypeHandler实现自己的加密解密算法

@Log4j2
public class AESTypeHandler extends CryptTypeHandler {
	
	private static ThreadLocal<String> KEY_HOLDER = new ThreadLocal<String>();
	
	public static void setKey(String key) {
		KEY_HOLDER.set(key);
	}
	public static String getKey() {
		return KEY_HOLDER.get();
	}
	public static void clearKey() {
		KEY_HOLDER.remove();
	}
	
	@Override
	protected String encrypt(String origin) {
		var key = getKey();
		if(StringUtils.hasText(key)) {
			try {
				return AESUtil.encrypt(origin, key);
			} catch (Exception e) {
				log.error(e);
			}finally {
				clearKey();
			}
		}
		return origin;
	}

	@Override
	protected String decrypt(String origin) {
		var key = getKey();
		if(StringUtils.hasText(key)) {
			try {
				return AESUtil.decrypt(origin, key);
			} catch (Exception e) {
				log.error(e);
			}finally {
				clearKey();
			}
		}
		return origin;
	}

}

```
##### CryptTypeHandler代码
```
@Log4j2
public abstract class CryptTypeHandler extends BaseTypeHandler<String>{

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
			throws SQLException {
		if(log.isDebugEnabled()) {
			log.debug("before encrypt parameter {}",parameter);
		}
        var encryptStr = encrypt(parameter);
        if(log.isDebugEnabled()) {
        	log.debug("after encrypt parameter {}",encryptStr);
        }
        ps.setString(i, encryptStr);
		
	}

	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		var s = rs.getString(columnName);
		if (!StringUtils.hasText(s))return null;
		if(log.isDebugEnabled()) {
        	log.debug("before decrypt parameter {}", s);
        }
		var r = decrypt(s);
		if(log.isDebugEnabled()) {
        	log.debug("after decrypt parameter {}", r);
        }
		return r;
	}

	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		var s = rs.getString(columnIndex);
		if (!StringUtils.hasText(s))return null;
		if(log.isDebugEnabled()) {
        	log.debug("before decrypt parameter {}", s);
        }
		var r = decrypt(s);
		if(log.isDebugEnabled()) {
        	log.debug("after decrypt parameter {}", r);
        }
		return r;
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		var s = cs.getString(columnIndex);
		if (!StringUtils.hasText(s))return null;
		if(log.isDebugEnabled()) {
        	log.debug("before decrypt parameter {}", s);
        }
		var r = decrypt(s);
		if(log.isDebugEnabled()) {
        	log.debug("after decrypt parameter {}", r);
        }
		return r;
	}
	
	protected abstract String encrypt (String origin);
	
	protected abstract String decrypt (String origin);

}

```

##### 测试
```
// 这里一定要注意，如果加密算法需要key，在执行数据库操作之前，一定要setKey,否则不会加密解密!!!!!!
@Before
public void before () {
	AESTypeHandler.setKey(key);
}
```

```
// 测试insert
@Test
public void testInsert () {
	var user = new User();
	user.setId("id1");
	user.setPassword("123456");//这里的密码是明文的
	userService.insertSelective(user);
	//id1,TqTLBNsLl67ZQgvQqbmiXElTIi9bEih3otxnolFclLk=
}
//最后插入到数据库里的数据是加密以后的
```

```
//测试select
public void testSelect() {
	var user = userService.selectById("id1");
	System.out.println(user.getPassword())
	//123456
}

//最后的结果是明文
```

#### 自定义查询

```
/**
 *	由于mapper走的是Mybatis标准，没有经过tkMapper，所以这里需要用mybatis的组件来指定typeHandler
 */
public interface UserMapper extends BaseMapper<User>{
	
	/**
	 * 通过对象封装
	 * @return
	 */
	@Results({
		@Result(column = "id",property = "id"),
		@Result(column = "password",property = "password",typeHandler = AESTypeHandler.class)
	})
	@Select("select * from t_patron_user limit 1")
	User selectUserByMapper();
	
	/**
	 * 通过map封装
	 * @return
	 */
	@Results({
		@Result(column = "id",property = "id"),
		@Result(column = "password",property = "password",typeHandler = AESTypeHandler.class)
	})
	@Select("select * from t_patron_user limit 1")
	Map<String,String> selectMapByMapper();

}
```

```
@Test
public void testSelectUserByMapper () {
	var user = userMapper.selectUserByMapper();
	System.out.println(JSONObject.toJSONString(user));
	//{"id":"id1","password":"123456"}
}
@Test
public void testSelectMappByMapper () {
	var map = userMapper.selectMapByMapper();
	System.out.println(JSONObject.toJSONString(map));
	//{"id":"id1","password":"123456"}
}
//最后的结果是明文
```

## MyBatis 自定义 JSONTypeHandler

### 使用场景

当我们的数据库的某个列保存的是JSON字符串，通常情况下，对应的mode会用String来接收，然后在程序里面手动转成对象，例如：


```

//mode
public class Activity {

	@Id
	private String activityId;
	
	/**
	  *   数据库里保存的是一个JSON字符串
	  */
	private String displaySetting;
}

//使用
public class ActivityService {
	
	/**
	  * 传统的做法是先用字符串接收，然后再收到转换成对象
	  */
	public DisplaySetting getDisplaySetting (String id) {
		Activity activity = selectById (id);
		return JSONObject.parseObject (activity.getDisplaySetting, DisplaySetting.class);
	}
	
}

```

现在我们可以有更优雅的写法


### 依赖

```
<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>spritest-boot-starter-mybatis</artifactId>
	<version>2.0.0-iota</version>
</dependency>
```

### 使用JSONTypeHandler

- ##### json转换工具是JacksonJson,因此有特殊需求的要用jackson相关注解，fastjson不生效

- ##### TypeHandler不能很好的支持泛型，如果泛型类型为基本数据类型，如： `List<Integer>, Map<String,String>` 可以支持，

- ##### 如果泛型类型为复杂类型，需要包装一层

```
	/**
	  * tk mapper已经支持了在mode里指定TypeHandler
	  */
	public class Activity {
		@Id
		private String activityId;
		
		/**
		  *	 如果类型为map，则用Display继承HashMap
		  */
		@ColumnType(typeHandler = JSONTypeHandler.class)
		private Display displaySetting;
		
		/**
		  *	如果泛型类型为基本类型，可以直接写
		  */
		@ColumnType(typeHandler = JSONTypeHandler.class)
		private List<Integer> bizType;
		
		/**
		 *  如果类型为 List，则用Configs集成ArrayList
		 */
		@ColumnType(typeHandler = JSONTypeHandler.class)
		private Configs configSetting;
}
  
    @Getter
    @Setter
    public static class DisplaySetting {
        private String label;

        private boolean displayInList;

        private boolean displayInAz;

        private String title;

        private String link;

        private String image;

        private Date startTime;

        private Date endTime;
    }
  
	/**
	  *自定义SQL，这里必须手动指定TypeHandler
	  */
	public interface ActivityMapper {

		@Results(
			{
			@Result(column = "activity_id",property = "activityId"),
			@Result(column = "display_setting",property = "DisplaySetting",typeHandler = JSONTypeHandler.class)
			}
		)
		@select{"select * from activity"}
		List<Activity> select();
	}
  
  
 //使用
public class ActivityService {
	
	/**
	  * 可以直接转成对象
	  */
	public List<Integer> getDisplaySetting (String id) {
		Activity activity = selectById (id);
		return activity.getBizeType();
	}
	
}
```

### 举例

#### 1.对于基本类型的转换
假如db中存储的数据是

```
[1,2]
```

那么经过

```
@ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
private List<Integer> bizType;
```

后，就返回了list数组，我们就可以直接使用数组了,同理 `List<String>, Map<String,String>`等也是可以的


#### 2.对于返回 `Map<String,DisplaySetting>`


-  对与这种复杂的泛型类型，需要创建 `Map<String,DisplaySetting>`的子类 Display, 不能直接用Map，因为泛型会被擦除，转JSON时不知道Map里面应该保存哪种类型，会被转成Map，DisplaySetting里面可以带任意类型的属性

db中字段存储格式如下：

```
{
    "wap":{
        "link":"2",
        "image":"1",
        "title":"2",
        "endTime":1596150000000,
        "startTime":1595199600000,
        "displayInAz":true,
        "displayInList":true
    },
    "android":{
        "link":"2",
        "image":"https://static-test1.com/public/1ee751c6-8b8f-4180-abc3-42287895c0c7.jpg",
        "title":"2",
        "endTime":1596150000000,
        "startTime":1595199600000,
        "displayInAz":false,
        "displayInList":false
    }
}
```

经过

```
@ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
private Display displaySetting; // (Display 为Map<String,DisplaySetting>子类)

public static class Display extends HashMap<String, DisplaySetting> {

}
```

返回后，就返回了Display对象可以当map用，"wap", "android"就是key，

后面的{}中的对象就会被转成DisplaySetting对象做为key的value供我们使用。

#### 3.转换`List<Config>`对象

- 也不能直接用List,也得创建 `List<Config>` 子类 Configs, Config里面可以是任意类型，带不带泛型都行

db中字段存储格式如下：

```
[
	{
        "val": "test",
		"displaySettings": [
			{
				"link":"2",
				"image":"https://static-test1.com/public/1ee751c6-8b8f-4180-abc3-42287895c0c7.jpg",
				"title":"2",
				"endTime":1596150000000,
				"startTime":1595199600000,
				"displayInAz":false,
				"displayInList":false
   			 }
		]
    }
]
```

经过

```
@ColumnType(typeHandler = JSONTypeHandler.class)
private Configs configSetting;

@Setter
@Getter
public static class Config {
	private String val;
	private List<DisplaySetting> displaySettings;
}

@NoArgsConstructor
public static class Configs extends ArrayList<Config> {

	public Configs(Collection<? extends Config> c) {
		super(c);
	}
}
```
返回后，就返回了Configs对象可以当list用，config对象里面可以是任意类型的List,Map


#### 4.转换Object对象
只需要db中存储的是json Object对象，在经过注解的转换后，就会按照类属性名相同的方式进行赋值，最后返回我们需要的xxxClass Object对象了。


## Mybatis自定义OBSTypeHandler

### 使用场景

数据库中需要保存大的json或者text字段，直接存在mysql中，影响数据库性能，因此，把json或者text保存到s3、dynamodb中，mysql保存对应key. 该插件可以自动做这些事情，我们使用时，正常的操作数据库，对s3 dynamodb操作无感知。

### S3TypeHandler

将数据保存到s3中
### maven依赖

```
<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>spritest-boot-starter-mybatis</artifactId>
</dependency>

<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>spritest-boot-starter-s3</artifactId>
</dependency>

<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>common-id-generator</artifactId>
</dependency>
```

#### application.properties配置

```
## jdbc配置...
jdbc.config.user.data-source[0].url=jjdbc:mysql://127.0.0.1:3306/xxx_xxx
...忽略

## s3配置，access key跟access key id
aws.s3.access-key=${aws.s3.access-key}
aws.s3.access-key-id=${aws.s3.access-key-id}
//s3桶
aws.s3.default-bucket=${aws.s3.default-bucket}
//s3 region
aws.s3.region=${aws.s3.region}
//s3保存的环境，这个配置很重要，决定要保存到哪个文件夹，线上配置online
aws.s3.profile=${aws.s3.profile}

## 因为生成的Key依赖于common-id-generator,因此需要添加redis配置
spring.redis.host=localhost
spring.redis.port=6379
```

#### 使用

跟 JSONTypeHandler类似，可以用在基本查询跟mapper中的自定义查询

- 实体类

```
@Table(name="t_order")
public class Order {
	
	@Id
	private String id;
	
	//如果类型是Object(规则跟JSONTypeHandler一样，对与list跟map需要封装成不带泛型的类)，可以自动序列化、反序列化
	@ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = S3TypeHandler.class)
	private OrderInfo orderInfo;
	
	//也可以直接用String接收
	@ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = S3TypeHandler.class)
	private String extralInfo;
	
 	@Getter
    @Setter
    public static class OrderInfo {
        private String matchId;

        private Integer market;
    }
}
```

- mapper

```
	//对与自定义查询，一定要用@Result, 并且指定TypeHandler. 但是我们可以不写全部字段，只写需要特殊处理的字段
    @Results(
            {
                    @Result(column = "order_info", property = "orderInfo", typeHandler = S3TypeHandler.class)
            }
    )
    @Select("select * from t_order where id = #{id}")
    Order selectByMapper (@Param("id") String id);
```

- 测试

```
@Test
public void create () {
	var order = new Order();
	order.setId("order1");
	var info = new Order.OrderInfo();
	info.setMarket(1);
	info.setMatchId("xxxxx111111111");
	order.setOrderInfo(info);
	order.setExtralInfo(“ddddddddddd”);
	orderMapper.insertSelective(order);
	//insert以后，mysql中order表里的order_info跟extral_info字段保存的是s3对应的地址
}

@Test
public void select () {
	Order order = orderMapper.selectByPrimaryKey("order1");
	Order order = orderMapper.selectByMapper("order1");
	//order中 orderInfo跟extralInfo会自动从s3获取反序列化为对象
}
```

#### s3保存目录
[img.png](img/img.png)

- 解读路径

`/opensabe-tech/test/ng/typehandler/`
- hopgegaming 是桶的名字
- test是profile 测试环境配置test, 线上配置online （aws.s3.profile=test）
- ng是国家，根据部署的国家（跟header里的operId没关系）分别对应ng,gh,ug,public因此public不要直接操作国家库
- typehandler是写死的目录，通过本功能上传的文件，都保持到这个文件夹下

### DynamoDbTypeHandler

将数据保存到dynamodb中

#### maven依赖

```
<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>spritest-boot-starter-mybatis</artifactId>
</dependency>

<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>spritest-boot-starter-dynamodb</artifactId>
</dependency>

<dependency>
	<groupId>io.github.opensabe-tech</groupId>
	<artifactId>common-id-generator</artifactId>
</dependency>
```

```
## jdbc配置...
jdbc.config.user.data-source[0].url=jjdbc:mysql://127.0.0.1:3306/opensabe_marketing
...忽略

## s3配置，access key跟access key id
aws_access_key_id=${aws_access_key_id}
aws_secret_access_key=${aws_secret_access_key}
//s3 region,默认
aws_region=${aws_region}
//s3保存的环境，这个配置很重要
aws_env=${aws_env}

## 因为生成的Key依赖于common-id-generator,因此需要添加redis配置
spring.redis.host=localhost
spring.redis.port=6379
```

#### 使用

- 实体类

使用方法跟S3TypeHandler相同，把typeHandler改为DynamoDbTypeHandler.class即可
```
@Getter
@Setter
@Table(name = "t_dynamodb_type_handler")
public class DynamodbPO {

    @Id
    private String id;

	//typeHandler指定为DynamoDbTypeHandler.class
    @ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = DynamoDbTypeHandler.class)
    private OrderInfo orderInfo;

    @Getter
    @Setter
    public static class OrderInfo {
        private String matchId;

        private Integer market;
    }
}
```


- mapper

```
public interface DynamodbTypeHandlerMapper extends BaseMapper<DynamodbPO> {

    @Results({
            @Result(column = "order_info", property = "orderInfo", typeHandler = DynamoDbTypeHandler.class)
    })
    @Select("select * from t_dynamodb_type_handler where id = #{id}")
    DynamodbPO selectByMapper (@Param("id") String id);
}

```
#### 表结构
- **必须提前建表**:在使用DynamoDbTypeHandler前必须保证对应环境(默认aws_region=eu-central-1)下有对应的表,表名如下
- 表名:`dynamodb_${aws_env}_${defaultOperId}_typehandler`,如`dynamodb_test_2_typehandler`,表示测试环境国家operId=2的表
- 表结构:id即数据库存的唯一id,value即大字段的值
  ![img_1.png](img/img_1.png)

### observation

#### 获取连接

事件名称：mysql.connection.connect

触发时机：从连接池获取连接

##### HighCardinalityKeyValues

| 属性  |类型| 备注                                                   |
| ------------ | ------------ |------------------------------------------------------|
| activeCount   | int  | 连接池当前连接数量（不包含当前获取到的）                                 |
| maxActive  | int | 连接池最大连接数                                             |
| maxWaitThread  | int | 最多允许多少个线程等待连接，默认-1,不限制                               |
| waitThread | int | 当前有多少个线程等待获取锁，当获取锁的线程(获取连接的线程)数量大于maxWaitThread时，会报错 |
| maxWaitTime  | int | 获取连接最大等待毫秒数                                          |
| createTime |timestamp| 获取成功后，该连接创建时间                                        |
| success | boolean | 是否获取成功                                               |

##### LowCardinalityKeyValues

无，为了减少内存，不设置LowCardinalityKeyValues,不暴露普罗米修斯

##### JFR

事件名称：Connection Pool Monitor
事件分类（所属文件夹）：observation.mybatis

|属性|备注|
| ------------ | ------------ |
|Connection Create Time|连接创建的时间|
|Connection Count|连接池中剩余连接数量|
|Connect Event Type|Connect,获取连接还是释放连接|

#### 释放连接

事件名称：mysql.connection.release

触发时机：释放连接到连接池

##### HighCardinalityKeyValues

| 属性  |类型| 备注  |
| ------------ | ------------ | ------------------------|
| activeCount   | int  | 连接池当前连接数量（不包含当前获取到的） |
| createTime |timestamp|获取成功后，该连接创建时间|
| success | boolean | 是否获取成功 |

##### LowCardinalityKeyValues

无

##### JFR

事件名称：Connection Pool Monitor
事件分类（所属文件夹）：observation.mybatis

|属性|备注|
| ------------ | ------------ |
|Connection Create Time|连接创建的时间|
|Connection Count|连接池中剩余连接数量|
|Connect Event Type|Close,获取连接还是释放连接|


#### SQL执行监控

事件名称：sql.execute.mapper
触发时机：执行mapper中的SQL时

##### HighCardinalityKeyValues

| 属性  |类型| 备注               |
| ------------ | ------------ |------------------|
| method    | string  | 执行SQL的Mapper中的方法名 |
| transactionId  |string| 如果SQL在事务中，显示事务的ID |
| success | boolean | 是否获取成功           |

##### LowCardinalityKeyValues

| 属性  |类型| 备注  |
| ------------ | ------------ | -------------- |
| method    | string  | 执行SQL的Mapper中的方法名 |


##### JFR

事件名称：SQL Execute Monitor
事件分类（所属文件夹）：observation.mybatis

|属性|备注|
| ------------ | ------------ |
|SQL Executed Method|执行SQL的Mapper中的方法名|
|Transaction Id|如果SQL在事务中，显示事务的ID|
|traceId||
|spanId||
| success | 是否获取成功 |


### 事务执行监控

事件名称：sql.execute.transaction
触发时机：执行事务时

#### HighCardinalityKeyValues

| 属性  |类型| 备注  |
| ------------ | ------------ | --------------- |
| method    | string  | 执行事务的service中的方法名 |
| transactionId  |string|事务的ID|
| success | boolean | 是否获取成功 |

#### LowCardinalityKeyValues

| 属性  |类型| 备注  |
| ------------ | ------------ | ----------- |
| method    | string  | 执行SQL的Mapper中的方法名 |


#### JFR

事件名称：SQL Execute Monitor
事件分类（所属文件夹）：observation.mybatis

|属性|备注|
| ------------ | ------------ |
|SQL Executed Method|执行SQL的Mapper中的方法名|
|Transaction Id|如果SQL在事务中，显示事务的ID|
|traceId||
|spanId||
| success | 是否获取成功 |

## Slow SQL 调整

1. 将 druid 慢 SQL 的日志级别配置，默认配置为 WARN
2. 添加 alarmIntervalInSeconds 和 alarmThreshold 配置，如果同一个慢 SQL 在 alarmIntervalInSeconds 内出现 alarmThreshold 才会输出一次报警
3. 默认是 30s 内出现 20 次以上
