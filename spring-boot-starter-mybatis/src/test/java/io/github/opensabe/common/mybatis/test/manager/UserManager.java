package io.github.opensabe.common.mybatis.test.manager;

import io.github.opensabe.common.mybatis.test.po.User;
import io.github.opensabe.common.mybatis.test.service.UserMapperService;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

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

    public void queryMuiltple (String id) {
        User user = User.builder().id(id).firstName("foo").lastName("bar").createTime(new Timestamp(System.currentTimeMillis())).build();
        userMapperService.insertSelective(user);
        //立刻查询，这时候应该能查询到
        userMapperService.selectById(id);
    }

    @Transactional
    public void testCommit (String id) {
        User user = User.builder().id(id).firstName("foo").lastName("bar").createTime(new Timestamp(System.currentTimeMillis())).build();
        userMapperService.insertSelective(user);
        //立刻查询，这时候应该能查询到
        userMapperService.selectById(id);
    }
}
