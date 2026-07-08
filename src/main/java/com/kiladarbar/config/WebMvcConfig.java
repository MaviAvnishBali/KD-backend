package com.kiladarbar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves locally-stored media (see {@link com.kiladarbar.service.S3Service} local mode)
 * at {@code /uploads/**}. With the {@code /api} context path this is exposed at
 * {@code /api/uploads/**}.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + storageProperties.getLocalDir().replaceAll("/+$", "") + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
