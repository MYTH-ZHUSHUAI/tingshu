package com.atguigu.tingshu.oss.config;

import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Resource
    private OssProperties ossProperties;

    @Bean
    @ConditionalOnProperty(name = "oss.provider", havingValue = "minio", matchIfMissing = true)
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(ossProperties.getMinio().getEndpointUrl())
                .credentials(ossProperties.getMinio().getAccessKey(), ossProperties.getMinio().getSecretKey())
                .build();
    }
}
