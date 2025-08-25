/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.mapper.user.UserMapper;
import io.github.opensabe.common.mybatis.test.po.User;
import io.github.opensabe.common.mybatis.test.service.UserMapperService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@DisplayName("只读数据源测试")
public class ReadOnlyTest extends BaseMybatisTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserMapperService userMapperService;
    
    @Test
    @DisplayName("测试Mapper只读数据源 - 验证主从分离")
    public void testMapperReadOnly () {
        userMapper.insertSelective(new User("ReadOnlyTest-testMapperReadOnly-id","first name","last name", new Timestamp(System.currentTimeMillis()),null));
        var list = userMapper.selectReadOnly();
        //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
        Assertions.assertEquals(0,list.size());
        var user = userMapper.selectByPrimaryKey("ReadOnlyTest-testMapperReadOnly-id");
        Assertions.assertNotNull(user);
    }
    
    @Test
    @DisplayName("测试基础服务只读数据源 - 验证主从分离")
    public void testBaseServiceReadOnly () {
        var record = new User("ReadOnlyTest-testBaseServiceReadOnly-id","first name","last name", new Timestamp(System.currentTimeMillis()),null);
        userMapperService.insertSelective(record);
        var list = userMapperService.selectByIdReadOnly("ReadOnlyTest-testBaseServiceReadOnly-id");
        //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
        Assertions.assertNull(list);
        var user = userMapperService.selectById("ReadOnlyTest-testBaseServiceReadOnly-id");
        Assertions.assertEquals(user, record);
    }

    @Test
    @DisplayName("测试循环只读数据源 - 验证主从分离的稳定性")
    public void testLoopReadOnly () {
        for (int i = 0; i < 10; i++) {
            userMapper.insertSelective(new User("ReadOnlyTest-testLoopReadOnly-id"+i,"first name","last name", new Timestamp(System.currentTimeMillis()),null));
            var list = userMapper.selectReadOnly();
            //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
            Assertions.assertEquals(list.size(),0);
            var user = userMapper.selectByPrimaryKey("ReadOnlyTest-testLoopReadOnly-id"+i);
            Assertions.assertNotNull(user);
            userMapper.deleteByPrimaryKey("ReadOnlyTest-testLoopReadOnly-id"+i);
        }
    }
    
    @Test
    @DisplayName("测试多线程只读数据源 - 验证并发场景下的主从分离")
    public void testThreadReadOnly () throws InterruptedException, ExecutionException {
        var executor = Executors.newFixedThreadPool(5);
        List<Future> list = new ArrayList<>(50);
        for (int i = 0; i < 50; i++) {
            var c = i;
            list.add(executor.submit(() -> {
                userMapper.insertSelective(new User("ReadOnlyTest-testThreadReadOnly-id"+c,"first name","last name", new Timestamp(System.currentTimeMillis()),null));
                var readOnly = userMapper.selectReadOnly();
                //因为docker的mysql并没有做主从复制，因此主库插入以后从库依然为空
                Assertions.assertEquals(readOnly.size(),0);
                var user = userMapper.selectByPrimaryKey("ReadOnlyTest-testThreadReadOnly-id"+c);
                Assertions.assertNotNull(user);
                userMapper.deleteByPrimaryKey("ReadOnlyTest-testThreadReadOnly-id"+c);
                return null;
            }));
        }
        for (Future future : list) {
            future.get();
        }
    }
}
