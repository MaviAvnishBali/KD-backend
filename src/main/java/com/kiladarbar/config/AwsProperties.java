package com.kiladarbar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {

    private String region = "ap-south-1";
    private String accessKey;
    private String secretKey;
    /** Custom S3-compatible endpoint (e.g. Oracle Object Storage). Blank = real AWS S3. */
    private String endpoint;
    private S3 s3 = new S3();

    @Data
    public static class S3 {
        private String bucket = "kiladarbar-media";
        private String cdnUrl;
    }
}
