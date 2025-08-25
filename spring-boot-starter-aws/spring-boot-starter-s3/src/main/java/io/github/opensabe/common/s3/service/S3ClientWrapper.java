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
package io.github.opensabe.common.s3.service;

import io.github.opensabe.common.s3.observation.S3OperationContext;
import io.github.opensabe.common.s3.observation.S3OperationConvention;
import io.github.opensabe.common.s3.observation.S3OperationObservationDocumentation;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static software.amazon.awssdk.core.sync.RequestBody.fromBytes;

@Log4j2
public record S3ClientWrapper(S3Client s3Client, String folderName, String bucketName,
                              UnifiedObservationFactory unifiedObservationFactory) {

    /**
     * inputStream to byte[]
     *
     * @param inputStream inputStream
     * @return byte[]
     * @throws IOException iOException
     */
    private byte[] convert(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int n;
        byte[] buf = new byte[1024];
        while ((n = inputStream.read(buf)) != -1) {
            byteArrayOutputStream.write(buf, 0, n);

        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 支持流上传
     *
     * @param inputStream 文件流
     * @return String 文件名
     * @throws IOException IOException
     */
    public String upload(InputStream inputStream) throws IOException {
        return upload(inputStream, null, null, null);
    }

    public String upload(byte[] bytes) {
        return upload(bytes, null, null, null);
    }

    public String upload(InputStream inputStream, String suffix) throws IOException {
        return upload(inputStream, suffix, null, null);
    }

    public String upload(byte[] bytes, String suffix) {
        return upload(bytes, suffix, null, null);
    }

    public String upload(InputStream inputStream, String suffix, String contentType, String folder) throws IOException {
        byte[] bytes = convert(inputStream);
        return upload(bytes, suffix, contentType, folder);
    }

    public String upload(byte[] bytes, String suffix, String contentType, String folder) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        md5.update(bytes);
        String md5Value = new BigInteger(1, md5.digest()).toString(16);
        String fileName = folderName + "/" + (StringUtils.isEmpty(folder) ? "" : folder + "/") + md5Value + (suffix == null ? "" : suffix);

        PutObjectRequest.Builder builder = PutObjectRequest.builder();
        if (contentType != null) {
            builder.contentType(contentType);
        }
        builder.key(fileName).bucket(bucketName);

        S3OperationContext s3OperationContext = new S3OperationContext(fileName, "getObject");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            s3Client.putObject(builder.build(), fromBytes(bytes));
            s3OperationContext.setSuccess(true);
            return fileName;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }

    }

    public String uploadWithOriginName(InputStream inputStream, String fileName, String contentType, String folder)
            throws IOException {
        byte[] bytes = convert(inputStream);
        return uploadWithOriginName(bytes, fileName, contentType, folder);
    }

    public String uploadWithOriginName(byte[] bytes, String name, String contentType, String folder) {
        String fileName = folderName + "/" + (StringUtils.isEmpty(folder) ? "" : folder + "/") + name;
        PutObjectRequest.Builder builder = PutObjectRequest.builder();
        if (contentType != null) {
            builder.contentType(contentType);
        }
        builder.key(fileName).bucket(bucketName);

        S3OperationContext s3OperationContext = new S3OperationContext(fileName, "getObject");
        s3OperationContext.setFileSize(bytes.length);
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            s3Client.putObject(builder.build(), fromBytes(bytes));
            s3OperationContext.setSuccess(true);
            return fileName;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 下载文件
     *
     * @param fileName 文件名(不含uri)
     * @return InputStream 输入流
     */
    public InputStream download(String fileName) {
        S3OperationContext s3OperationContext = new S3OperationContext(fileName, "getObject");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderName + "/" + fileName)
                    .build();
            InputStream inputStream = s3Client.getObject(getObjectRequest);

            s3OperationContext.setSuccess(true);
            return inputStream;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }


    /**
     * 根据文件名判断文件是否已经存在
     *
     * @param fileName
     * @return
     */
    public boolean doesObjectExists(String fileName) {
        fileName = folderName + "/" + fileName;
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(fileName).build();

        S3OperationContext s3OperationContext = new S3OperationContext(fileName, "headObject");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            s3Client.headObject(headObjectRequest);
            s3OperationContext.setSuccess(true);
            return true;
        } catch (NoSuchKeyException e) {
            s3OperationContext.setSuccess(false);
            return false;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 复制文件
     *
     * @param sourceFileName      源文件
     * @param destinationFileName 目标文件
     */
    public void copy(String sourceFileName, String destinationFileName) {
        S3OperationContext s3OperationContext = new S3OperationContext(sourceFileName + "_" + destinationFileName, "copyObject");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder().copySource("/" + bucketName + "/" + folderName + "/" + sourceFileName)
                    .destinationBucket(bucketName)
                    .destinationKey(folderName + "/" + destinationFileName).build();
            s3Client.copyObject(copyObjectRequest);
            s3OperationContext.setSuccess(true);
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

}
