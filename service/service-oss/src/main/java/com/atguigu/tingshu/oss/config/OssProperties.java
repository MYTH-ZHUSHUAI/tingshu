package com.atguigu.tingshu.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private String provider = "minio";

    private Minio minio = new Minio();
    private Aliyun aliyun = new Aliyun();
    private Tencent tencent = new Tencent();

    @Data
    public static class Minio {
        private String endpointUrl;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private boolean publicRead = true;
    }

    @Data
    public static class Aliyun {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }

    @Data
    public static class Tencent {
        private String secretId;
        private String secretKey;
        private String region;
        private String bucketName;
    }
}
