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
package io.github.opensabe.common.s3.test.common;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.testcontainers.integration.SingleS3IntegrationTest;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@Log4j2
@ExtendWith({SpringExtension.class, SingleS3IntegrationTest.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "aws.s3.folderName=" + S3BaseTest.FOLDER_NAME,
        "aws.s3.defaultBucket=" + S3BaseTest.BUCKET_NAME,
        "aws.s3.profile=test"
}, classes = App.class)
@AutoConfigureObservability
public abstract class S3BaseTest {
    public static final String FOLDER_NAME = "testFolder/country";
    public static final String BUCKET_NAME = "test-bucket";
    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Properties s3Properties;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleS3IntegrationTest.setProperties(registry);
    }

    @BeforeEach
    public void initializeBucket() {
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        Optional<Bucket> first = listBucketsResponse.buckets().stream().filter(bucket1 -> StringUtils.equals(bucket1.name(), s3Properties.getDefaultBucket())).findFirst();
        if (!first.isPresent()) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.getDefaultBucket()).build());
        }
    }
}
