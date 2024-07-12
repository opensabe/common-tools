package io.github.opensabe.common.s3.test;

import io.github.opensabe.common.s3.service.S3ClientWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

public class S3ClientWrapperTest extends S3BaseTest {

    @Autowired
    private S3ClientWrapper s3ClientWrapper;

    byte[] bytes;

    public S3ClientWrapperTest() throws Exception {
        bytes = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("funny-cat.jpeg")).readAllBytes();
    }

    @Test
    public void testUploadFile() throws IOException {
        String upload = s3ClientWrapper.upload(bytes);
        try (InputStream download = s3ClientWrapper.download(upload.replaceAll(FOLDER_NAME + "/", ""))) {
            byte[] allBytes = download.readAllBytes();
            Assertions.assertEquals(Base64.getEncoder().encodeToString(allBytes), Base64.getEncoder().encodeToString(bytes));
        }
    }

    @Test
    public void testUploadFileWithName() throws IOException {
        s3ClientWrapper.uploadWithOriginName(bytes, "funny-cat.jpeg", "image/jpeg", null);
        try (InputStream download = s3ClientWrapper.download("funny-cat.jpeg")){
            byte[] allBytes = download.readAllBytes();
            Assertions.assertEquals(Base64.getEncoder().encodeToString(allBytes), Base64.getEncoder().encodeToString(bytes));
        }
    }

    @Test
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
