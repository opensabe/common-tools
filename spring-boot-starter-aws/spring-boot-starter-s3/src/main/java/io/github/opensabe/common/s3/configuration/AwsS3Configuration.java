package io.github.opensabe.common.s3.configuration;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.s3.jfr.S3OperationObservationToJFRGenerator;
import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.AsyncTaskFileService;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.s3.service.S3SyncFileService;
import io.github.opensabe.common.s3.typehandler.S3OBSService;
import io.github.opensabe.common.s3.service.S3AsyncTaskFileService;
import io.github.opensabe.common.s3.service.S3ClientWrapper;
import io.github.opensabe.common.typehandler.OBSService;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

import static software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create;

@Log4j2
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "aws.s3.enabled", havingValue = "true", matchIfMissing = true)
public class  AwsS3Configuration {

	private final S3Properties s3Properties;
	@Autowired
	private ThreadPoolFactory threadPoolFactory;
	@Value("${awsS3LocalUrl:}")
	private String awsS3LocalUrl;

	@Autowired(required = false)
	public AwsS3Configuration(S3Properties s3Properties) {
		this.s3Properties = s3Properties;
	}

	@Bean("s3SyncClient")
	public S3Client s3SyncClient() {
	    log.info("s3 sync client inits...");
		S3ClientBuilder builder = S3Client.builder();
		builder.region(Region.of(s3Properties.getRegion()))
        .credentialsProvider(() -> create(s3Properties.getAccessKeyId(),s3Properties.getAccessKey()));
		if(StringUtils.isNotEmpty(awsS3LocalUrl)){
			log.fatal("AwsS3 will use local url {}",awsS3LocalUrl);
			builder.endpointOverride(URI.create(awsS3LocalUrl));
		}
		//proxy 配置
//        if(StringUtils.isNotBlank(s3Properties.getEndpoint())) {
//        	builder.httpClient(ApacheHttpClient.builder()
//                    .proxyConfiguration(ProxyConfiguration.builder()
//                            .endpoint(URI.create(s3Properties.getEndpoint()))
//                            .build())
//                    .build());
//        }
        try (S3Client client = builder.build()){
            log.info("s3 sync client is inited: " + client.listBuckets().buckets());
        } catch (Throwable e) {
            log.error("s3 sync client init failed!", e);
        }
		return builder.build();
	}

	@Bean
	public S3ClientWrapper getS3ClientWrapper(S3Properties s3Properties, S3Client s3Client, UnifiedObservationFactory unifiedObservationFactory) {
		return new S3ClientWrapper(s3Client, s3Properties.getFolderName(), s3Properties.getDefaultBucket(),unifiedObservationFactory);
	}

	@Bean("s3ObjectSyncFileService")
	public FileService s3ObjectSyncFileService(@Qualifier("s3SyncClient")S3Client client, UnifiedObservationFactory unifiedObservationFactory) {
		S3SyncFileService service = new S3SyncFileService(unifiedObservationFactory);
		service.setClient(client);
		service.setDefaultBucket(s3Properties.getDefaultBucket());
		return service;
	}

	/**
	 * 暂时用同步上传的service,异步上传本地无法设置代理，以后解决
	 * @param s3ObjectSyncFileService
	 * @return
	 */
	@Bean
	public AsyncTaskFileService asyncFileService(@Qualifier("s3ObjectSyncFileService") FileService s3ObjectSyncFileService) {
		var executorService = threadPoolFactory.createNormalThreadPool("s3-async task-", 8);
		return new S3AsyncTaskFileService(s3ObjectSyncFileService,executorService,s3Properties);
	}

	@Bean
	public OBSService s3OBSService (@Qualifier("s3ObjectSyncFileService")FileService fileService,
									S3Properties properties,
									@Value("${defaultOperId:0}") String defaultOrderId) {
		return new S3OBSService(fileService, properties,defaultOrderId);
	}

	@Bean
	public S3OperationObservationToJFRGenerator s3OperationObservationToJFRGenerator( ) {
		return new S3OperationObservationToJFRGenerator();
	}


}
