package io.github.opensabe.common.web.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.google.common.collect.Lists;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ConverterConfiguration {
    @SuppressWarnings("deprecation")
    @Bean
    public HttpMessageConverters httpMessageConverters() {
        //创建FastJson信息转换对象
        RevoFastJsonHttpMessageConverter fastJsonHttpMessageConverter =
                new RevoFastJsonHttpMessageConverter();

        List<MediaType> supportedMediaTypes = Lists.newArrayList();
        //从1.1.41升级到1.2.之后的版本必须配置，否则会报错
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        //创建FastJson对象并设定序列化规则
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //添加自定义valueFilter
        //规则赋予转换对象
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        StringHttpMessageConverter stringHttpMessageConverter =
                new StringHttpMessageConverter(Charset.defaultCharset());
        stringHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_PLAIN));
        fastJsonConfig.setSerializerFeatures(
                //消除对同一对象循环引用的问题，默认为false（如果不配置有可能会进入死循环）
                SerializerFeature.DisableCircularReferenceDetect,
                //是否输出值为null的字段,默认为false
                SerializerFeature.WriteMapNullValue,
                //是否双引号包装field
                SerializerFeature.QuoteFieldNames
        );

        return new HttpMessageConverters(fastJsonHttpMessageConverter, stringHttpMessageConverter);
    }
}
