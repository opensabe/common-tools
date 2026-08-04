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
package io.github.opensabe.common.s3.configuration;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.s3.jfr.S3OperationObservationToJFRGenerator;
import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.AsyncTaskFileService;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.s3.service.S3AsyncTaskFileService;
import io.github.opensabe.common.s3.service.S3ClientWrapper;
import io.github.opensabe.common.s3.service.S3SyncFileService;
import io.github.opensabe.common.s3.typehandler.S3OBSService;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import static software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create;

/**
 * AWS S3 同步客户端与文件服务 Bean 配置。
 * <p>
 * SDK 2.30+ 默认 CRC32 校验；此处显式 {@code WHEN_REQUIRED} 以兼容 LocalStack 等端点。
 * 异步上传暂委托同步 {@link S3SyncFileService}（本地代理限制，见 {@link #asyncFileService}）。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "aws.s3.enabled", havingValue = "true", matchIfMissing = true)
public class AwsS3Configuration {

    /** S3 连接与桶配置。 */
    private final S3Properties s3Properties;

    /** 异步任务线程池工厂。 */
    @Autowired
    private ThreadPoolFactory threadPoolFactory;

    /** 可选本地/兼容 S3 端点 URL（{@code awsS3LocalUrl}）。 */
    @Value("${awsS3LocalUrl:}")
    private String awsS3LocalUrl;

    /**
     * @param s3Properties S3 配置；无 Bean 时可为 {@code null}（{@code required = false}）
     */
    @Autowired(required = false)
    public AwsS3Configuration(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
    }

    /**
     * 构建同步 {@link S3Client} 并探测连通性（listBuckets）。
     * <p>
     * 注意：探测与返回的 client 为两次 {@code build()}，探测失败仅打日志。
     *
     * @return 同步 S3 客户端
     */
    @Bean("s3SyncClient")
    public S3Client s3SyncClient() {
        log.info("Initializing S3 sync client...");
        S3ClientBuilder builder = S3Client.builder();
        builder.region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(() -> create(s3Properties.getAccessKeyId(), s3Properties.getAccessKey()))
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);
        if (StringUtils.isNotEmpty(awsS3LocalUrl)) {
            log.warn("S3 client using custom endpoint: {}", awsS3LocalUrl);
            builder.endpointOverride(URI.create(awsS3LocalUrl));
        }
        try (S3Client client = builder.build()) {
            log.info("S3 sync client connectivity check: buckets={}", client.listBuckets().buckets());
        } catch (Throwable e) {
            log.error("S3 sync client init connectivity check failed", e);
        }
        return builder.build();
    }

    /**
     * 带观测的 S3 客户端包装。
     *
     * @param s3Properties 桶与路径配置
     * @param s3Client 底层同步客户端
     * @param unifiedObservationFactory 观测工厂
     * @return 包装客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public S3ClientWrapper getS3ClientWrapper(S3Properties s3Properties, S3Client s3Client, UnifiedObservationFactory unifiedObservationFactory) {
        return new S3ClientWrapper(s3Client, s3Properties.getFolderName(), s3Properties.getDefaultBucket(), unifiedObservationFactory);
    }

    /**
     * 同步文件服务 Bean。
     *
     * @param client 同步 S3 客户端
     * @param unifiedObservationFactory 观测工厂
     * @return {@link S3SyncFileService}
     */
    @Bean("s3ObjectSyncFileService")
    @ConditionalOnMissingBean
    public FileService s3ObjectSyncFileService(@Qualifier("s3SyncClient") S3Client client, UnifiedObservationFactory unifiedObservationFactory) {
        S3SyncFileService service = new S3SyncFileService(unifiedObservationFactory);
        service.setClient(client);
        service.setDefaultBucket(s3Properties.getDefaultBucket());
        return service;
    }

    /**
     * 异步任务文件服务；当前基于同步 {@link FileService} 在线程池中执行。
     * <p>
     * 异步 SDK 上传暂无法在本地环境配置 HTTP 代理，后续可替换为原生异步实现。
     *
     * @param s3ObjectSyncFileService 同步文件服务
     * @return 异步任务包装
     */
    @Bean
    @ConditionalOnMissingBean
    public AsyncTaskFileService asyncFileService(@Qualifier("s3ObjectSyncFileService") FileService s3ObjectSyncFileService) {
        var executorService = threadPoolFactory.createNormalThreadPool("s3-async task-", 8);
        return new S3AsyncTaskFileService(s3ObjectSyncFileService, executorService, s3Properties);
    }

    /**
     * OBS 风格 S3 访问服务。
     *
     * @param fileService 文件服务
     * @param properties S3 配置
     * @param defaultOrderId 默认操作 ID（{@code defaultOperId}）
     * @return {@link S3OBSService}
     */
    @Bean
    @ConditionalOnMissingBean
    public S3OBSService s3OBSService(@Qualifier("s3ObjectSyncFileService") FileService fileService,
                                     S3Properties properties,
                                     @Value("${defaultOperId:0}") String defaultOrderId) {
        return new S3OBSService(fileService, properties, defaultOrderId);
    }

    /**
     * 将 S3 operation observation 转为 JFR 事件。
     *
     * @return JFR 生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public S3OperationObservationToJFRGenerator s3OperationObservationToJFRGenerator() {
        return new S3OperationObservationToJFRGenerator();
    }
}
