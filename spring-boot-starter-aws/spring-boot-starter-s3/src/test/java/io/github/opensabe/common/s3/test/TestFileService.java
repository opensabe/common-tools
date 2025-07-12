package io.github.opensabe.common.s3.test;

import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.s3.test.common.S3BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

@DisplayName("S3文件服务测试")
public class TestFileService extends S3BaseTest {

    @Autowired
    private FileService fileService;

    private final byte[] bytes;

    public TestFileService() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("funny-cat.jpeg")){
            this.bytes = inputStream.readAllBytes();
        }
    }

    @Test
    @DisplayName("测试文件上传和获取 - 验证文件服务功能")
    void testUpload () {
        String  path = fileService.putObject(bytes,"test", "fileServiceTest.jpeg");
        var bs = fileService.getObject(path);
        Assertions.assertNotNull(bs);
    }

}
