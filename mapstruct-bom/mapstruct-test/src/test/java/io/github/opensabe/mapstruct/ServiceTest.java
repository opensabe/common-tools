package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceTest {

    @Test
    void testTransform () {
        User user = new User();
        user.setAge(10);
        user.setName("martin");

        CommonCopyMapper<User, Student> mapper = MapperRepository.getInstance().getMapper(User.class, Student.class);
        Student student = mapper.map(user);
        Assertions.assertNotNull(student);
        Assertions.assertEquals(10, student.getAge());
        Assertions.assertEquals("martin", student.getName());
    }
}
