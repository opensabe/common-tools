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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static software.amazon.awssdk.core.sync.RequestBody.fromBytes;
import static software.amazon.awssdk.core.sync.RequestBody.fromFile;

/**
 * s3文件上传service
 *
 * @author maheng
 */
public class S3SyncFileService extends BucketS3FileService {

    private S3Client client;
    private final UnifiedObservationFactory unifiedObservationFactory;

    public S3SyncFileService(UnifiedObservationFactory unifiedObservationFactory) {
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public String putObject(File file, String bucket, String profile, String fileName) {
        String key = format("%s/%s", profile, fileName);
        var contentType = contentType(fileName);

        S3OperationContext s3OperationContext = new S3OperationContext(key, "putObject");
        s3OperationContext.setFileSize(file.length());
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build(), fromFile(file));
            s3OperationContext.setSuccess(true);
            return key;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public String putObject(byte[] source, String bucket, String profile, String fileName) {
        String key = format("%s/%s", profile, fileName);
        var contentType = contentType(fileName);

        S3OperationContext s3OperationContext = new S3OperationContext(key, "putObject");
        s3OperationContext.setFileSize(source.length);
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    //.acl(ObjectCannedACL.PUBLIC_READ)
                    .build(), fromBytes(source));
            s3OperationContext.setSuccess(true);
            return key;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public void putObjectAssignedPath(byte[] source, String fileName, String contentType) {
        S3OperationContext s3OperationContext = new S3OperationContext(fileName, "putObject");
        s3OperationContext.setFileSize(source.length);
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            client.putObject(PutObjectRequest.builder()
                    .bucket(getDefaultBucket())
                    .key(fileName)
                    .contentType(contentType)
                    .build(), fromBytes(source));
            s3OperationContext.setSuccess(true);
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public byte[] getObject(String key, String bucket) {
        S3OperationContext s3OperationContext = new S3OperationContext(key, "getObject");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            ResponseInputStream<GetObjectResponse> input = client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            byte[] bs = IOUtils.toByteArray(input);
            input.close();
            s3OperationContext.setFileSize(bs.length);
            s3OperationContext.setSuccess(true);
            return bs;
        } catch (Throwable e) {
            s3OperationContext.setSuccess(false);
            observation.error(e);
            throw new RuntimeException(e);
        } finally {
            observation.stop();
        }
    }


    @Override
    public List<String> listObjects(String basePath, String bucket) {
        S3OperationContext s3OperationContext = new S3OperationContext(basePath, "listObjects");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            List<String> list= client.listObjects(ListObjectsRequest.builder()
                            .bucket(bucket)
                            .marker(basePath)
                            .build())
                    .contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList())
                    ;
            s3OperationContext.setSuccess(true);
            s3OperationContext.setFileSize(CollectionUtils.isEmpty(list) ? 0 : list.size());
            return list;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public List<String> listObjectsByPrefix(String prefix, String bucket) {
        S3OperationContext s3OperationContext = new S3OperationContext(prefix, "listObjects");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            List<String> list= client.listObjects(ListObjectsRequest.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .build())
                    .contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList())
                    ;
            s3OperationContext.setSuccess(true);
            return list;
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public void deleteObject(String key, String bucket) {
        S3OperationContext s3OperationContext = new S3OperationContext(key, "deleteObject");
        Observation observation = S3OperationObservationDocumentation.S3_OPERATION.observation(
                null, S3OperationConvention.DEFAULT,
                () -> s3OperationContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            s3OperationContext.setSuccess(true);
        } catch (Throwable t) {
            s3OperationContext.setSuccess(false);
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    public S3Client getClient() {
        return client;
    }

    public void setClient(S3Client client) {
        this.client = client;
    }


    //	public static void main(String[] args) {
//		System.out.println("aaa.png".matches(".*\\.(png|jpg)"));
//		System.out.println("aaa.jpg".matches(".*\\.(png|jpg)"));
//		System.out.println("aaa.xl".matches(".*\\.(png|jpg)"));
//	}
    public String contentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        var arr = fileName.split("\\.");
        if (arr.length > 1) {
            if (arr[1].matches("(png|jpg|bmp|gif|jpeg|svg|targa|tiff|)")) {
                if ("svg".equals(arr[1])) {
                    return "image/svg+xml";
                }
                return "image/" + arr[1];
            }
        }
        return null;
    }
}
