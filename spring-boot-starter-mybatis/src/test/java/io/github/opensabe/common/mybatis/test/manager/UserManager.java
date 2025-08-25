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
package io.github.opensabe.common.mybatis.test.manager;

import java.sql.Timestamp;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.opensabe.common.mybatis.test.po.User;
import io.github.opensabe.common.mybatis.test.service.UserMapperService;

@Service
public class UserManager {
    @Autowired
    private UserMapperService userMapperService;

    @Transactional(rollbackFor = Exception.class)
    public void testTransactionRollback(String id) {
        //插入一个记录
        User user = User.builder().id(id).firstName("foo").lastName("bar").createTime(new Timestamp(System.currentTimeMillis())).build();
        userMapperService.insertSelective(user);
        //立刻查询，这时候应该能查询到
        User selectById = userMapperService.selectById(id);
        Assert.assertNotNull(selectById);
        //抛出异常，应该会 rollback
        throw new IllegalArgumentException();
    }

    public void queryMuiltple(String id) {
        User user = User.builder().id(id).firstName("foo").lastName("bar").createTime(new Timestamp(System.currentTimeMillis())).build();
        userMapperService.insertSelective(user);
        //立刻查询，这时候应该能查询到
        userMapperService.selectById(id);
    }

    @Transactional
    public void testCommit(String id) {
        User user = User.builder().id(id).firstName("foo").lastName("bar").createTime(new Timestamp(System.currentTimeMillis())).build();
        userMapperService.insertSelective(user);
        //立刻查询，这时候应该能查询到
        userMapperService.selectById(id);
    }
}
