package com.kiladarbar.service;

import com.kiladarbar.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    /**
     * Uploads a file to S3 under the given folder prefix and returns the public URL.
     *
     * @param folder    e.g. "menu-items/abc-uuid"
     * @param file      the uploaded multipart file
     * @return          full public URL of the stored object
     */
    public String upload(String folder, MultipartFile file) {
        String ext      = getExtension(file.getOriginalFilename());
        String key      = folder + "/" + UUID.randomUUID() + ext;
        String bucket   = awsProperties.getS3().getBucket();
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(mimeType)
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Uploaded {} to s3://{}/{}", file.getOriginalFilename(), bucket, key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        return buildUrl(bucket, key);
    }

    public void delete(String url) {
        String key = extractKey(url);
        if (key == null) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .build());
        log.info("Deleted s3 object: {}", key);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String buildUrl(String bucket, String key) {
        String cdn = awsProperties.getS3().getCdnUrl();
        if (cdn != null && !cdn.isBlank()) {
            return cdn.stripTrailing() + "/" + key;
        }
        String region = awsProperties.getRegion();
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String extractKey(String url) {
        // Works for both CDN and direct S3 URLs — everything after the first "/" following the host
        try {
            return url.replaceAll("^https?://[^/]+/", "");
        } catch (Exception e) {
            return null;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return "." + filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
