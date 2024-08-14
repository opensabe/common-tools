package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.mapper.user.UserMapper;
import io.github.opensabe.common.mybatis.test.po.User;
import io.github.opensabe.common.mybatis.test.service.UserMapperService;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReadOnlyTest extends BaseDataSourceTest {


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserMapperService userMapperService;
    @Test
    public void testMapperReadOnly () {
        userMapper.insertSelective(new User("id","first name","last name", new Timestamp(System.currentTimeMillis()),null));
        var list = userMapper.selectReadOnly();
        //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
        Assertions.assertEquals(0,list.size());
        var user = userMapper.selectByPrimaryKey("id");
        Assertions.assertNotNull(user);
    }
    @Test
    public void testBaseServiceReadOnly () {
        var record = new User("id","first name","last name", new Timestamp(System.currentTimeMillis()),null);
        userMapperService.insertSelective(record);
        var list = userMapperService.selectByIdReadOnly("id");
        //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
        Assertions.assertNull(list);
        var user = userMapperService.selectById("id");
        Assertions.assertEquals(user, record);
    }

    @Test
    public void testLoopReadOnly () {
        for (int i = 0; i < 10; i++) {
            userMapper.insertSelective(new User("id"+i,"first name","last name", new Timestamp(System.currentTimeMillis()),null));
            var list = userMapper.selectReadOnly();
            //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
            Assertions.assertEquals(list.size(),0);
            var user = userMapper.selectByPrimaryKey("id"+i);
            Assertions.assertNotNull(user);
        }
    }
    @Test
    public void testThreadReadOnly () throws InterruptedException, ExecutionException {
        var executor = Executors.newFixedThreadPool(5);
        List<Future> list = new ArrayList<>(50);
        for (int i = 0; i < 50; i++) {
            var c = i;
            list.add(executor.submit(() -> {
                userMapper.insertSelective(new User("id"+c,"first name","last name", new Timestamp(System.currentTimeMillis()),null));
                var readOnly = userMapper.selectReadOnly();
                //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
                Assertions.assertEquals(readOnly.size(),0);
                var user = userMapper.selectByPrimaryKey("id"+c);
                Assertions.assertNotNull(user);
                return null;
            }));
        }
        for (Future future : list) {
            future.get();
        }
    }
}
