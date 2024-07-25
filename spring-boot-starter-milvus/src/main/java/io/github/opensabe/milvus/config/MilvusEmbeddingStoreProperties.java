package io.github.opensabe.milvus.config;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.milvus.common.clientenum.ConsistencyLevelEnum.STRONG;

@Getter
@Setter
@ConfigurationProperties(prefix = MilvusEmbeddingStoreProperties.PREFIX)
public class MilvusEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.milvus";
    static final ConsistencyLevelEnum DEFAULT_CONSISTENCY_LEVEL = STRONG;

//    private String host;
//    private Integer port;
    private String collectionName;
    private Integer dimension;
    private IndexType indexType;
    private MetricType metricType;

    /**
     * aws中zilliz的Public Endpoint值
     */
    private String uri;

    /**
     * aws中zilliz的Token值
     */
    private String token;

//    private String username;
//    private String password;
    private ConsistencyLevelEnum consistencyLevel;
    private Boolean retrieveEmbeddingsOnSearch;
    private Boolean autoFlushOnInsert;
    private String databaseName;

    /**
     * 增加配置，用来控制是否启用量化的EmbeddingModel，默认不起用量化
     */
    private Boolean quantized = false;
}
