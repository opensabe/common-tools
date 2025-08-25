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
package io.github.opensabe.common.s3.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.s3.test.common.S3BaseTest;

@DisplayName("S3文件服务测试")
public class TestFileService extends S3BaseTest {

    private final byte[] bytes;
    @Autowired
    private FileService fileService;

    public TestFileService() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("funny-cat.jpeg")) {
            this.bytes = inputStream.readAllBytes();
        }
    }

    @Test
    @DisplayName("测试文件上传和获取 - 验证文件服务功能")
    void testUpload() {
        String path = fileService.putObject(bytes, "test", "fileServiceTest.jpeg");
        var bs = fileService.getObject(path);
        Assertions.assertNotNull(bs);
    }

}
