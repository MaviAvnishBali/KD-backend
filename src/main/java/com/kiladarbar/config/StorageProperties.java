package com.kiladarbar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Local filesystem media storage — used when AWS S3 is not configured
 * (i.e. no endpoint and no access key, as in local dev).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** Directory on disk where uploaded media is written. */
    private String localDir = "/tmp/kila-uploads";

    /** Public base URL these files are served from (see WebMvcConfig /uploads/** handler). */
    private String publicBaseUrl = "http://localhost:8080/api/uploads";
}
