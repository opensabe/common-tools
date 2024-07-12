package io.github.opensabe.common.s3.test;

import io.github.opensabe.common.s3.service.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

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
    void testUpload () {
        String  path = fileService.putObject(bytes,"test", "fileServiceTest.jpeg");
        var bs = fileService.getObject(path);
        Assertions.assertNotNull(bs);
    }

}
