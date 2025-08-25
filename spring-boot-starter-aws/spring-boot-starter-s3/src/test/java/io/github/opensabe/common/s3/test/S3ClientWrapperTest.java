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

import io.github.opensabe.common.s3.service.S3ClientWrapper;
import io.github.opensabe.common.s3.test.common.S3BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

@DisplayName("S3客户端包装器测试")
public class S3ClientWrapperTest extends S3BaseTest {

    @Autowired
    private S3ClientWrapper s3ClientWrapper;

    byte[] bytes;

    public S3ClientWrapperTest() throws Exception {
        bytes = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("funny-cat.jpeg")).readAllBytes();
    }

    @Test
    @DisplayName("测试文件上传和下载 - 验证数据完整性")
    public void testUploadFile() throws IOException {
        String upload = s3ClientWrapper.upload(bytes);
        try (InputStream download = s3ClientWrapper.download(upload.replaceAll(FOLDER_NAME + "/", ""))) {
            byte[] allBytes = download.readAllBytes();
            Assertions.assertEquals(Base64.getEncoder().encodeToString(allBytes), Base64.getEncoder().encodeToString(bytes));
        }
    }

    @Test
    @DisplayName("测试指定文件名上传 - 验证原始文件名保存")
    public void testUploadFileWithName() throws IOException {
        s3ClientWrapper.uploadWithOriginName(bytes, "funny-cat.jpeg", "image/jpeg", null);
        try (InputStream download = s3ClientWrapper.download("funny-cat.jpeg")){
            byte[] allBytes = download.readAllBytes();
            Assertions.assertEquals(Base64.getEncoder().encodeToString(allBytes), Base64.getEncoder().encodeToString(bytes));
        }
    }

    @Test
    @DisplayName("测试对象存在性检查和复制 - 验证文件操作")
    public void testDoesObjectExistsAndCopy() throws IOException {
        String fileName = "funny-cat-" + System.currentTimeMillis() + ".jpeg";
        boolean doesObjectExists = s3ClientWrapper.doesObjectExists(fileName);
        Assertions.assertFalse(doesObjectExists);
        s3ClientWrapper.uploadWithOriginName(bytes, fileName, "image/jpeg", null);
        doesObjectExists = s3ClientWrapper.doesObjectExists(fileName);
        Assertions.assertTrue(doesObjectExists);
        s3ClientWrapper.copy(fileName, "funny-cat-copy.jpeg");
        try (InputStream download = s3ClientWrapper.download("funny-cat-copy.jpeg")) {
            byte[] allBytes = download.readAllBytes();
            Assertions.assertEquals(Base64.getEncoder().encodeToString(allBytes), Base64.getEncoder().encodeToString(bytes));
        }
    }
}
