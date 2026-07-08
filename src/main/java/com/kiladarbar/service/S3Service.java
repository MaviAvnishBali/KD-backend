package com.kiladarbar.service;

import com.kiladarbar.config.AwsProperties;
import com.kiladarbar.config.StorageProperties;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;
    private final StorageProperties storageProperties;

    /** True when no S3-compatible target is configured (local dev) → store on local disk. */
    private boolean isLocalMode() {
        String endpoint = awsProperties.getEndpoint();
        String key = awsProperties.getAccessKey();
        return (endpoint == null || endpoint.isBlank()) && (key == null || key.isBlank());
    }

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

        if (isLocalMode()) {
            return uploadLocal(key, file);
        }

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
        if (isLocalMode()) {
            deleteLocal(url);
            return;
        }
        String key = extractKey(url);
        if (key == null) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .build());
        log.info("Deleted s3 object: {}", key);
    }

    // ── local filesystem (dev) ────────────────────────────────────────────────

    private String uploadLocal(String key, MultipartFile file) {
        try {
            Path dest = Paths.get(storageProperties.getLocalDir(), key);
            Files.createDirectories(dest.getParent());
            try (var in = file.getInputStream()) {
                Files.copy(in, dest);
            }
            String url = storageProperties.getPublicBaseUrl().replaceAll("/+$", "") + "/" + key;
            log.info("Stored upload locally at {} → {}", dest, url);
            return url;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file locally", e);
        }
    }

    private void deleteLocal(String url) {
        try {
            String base = storageProperties.getPublicBaseUrl().replaceAll("/+$", "") + "/";
            if (url == null || !url.startsWith(base)) return;
            Path path = Paths.get(storageProperties.getLocalDir(), url.substring(base.length()));
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete local file for {}: {}", url, e.getMessage());
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String buildUrl(String bucket, String key) {
        String cdn = awsProperties.getS3().getCdnUrl();
        if (cdn != null && !cdn.isBlank()) {
            return cdn.stripTrailing() + "/" + key;
        }
        String endpoint = awsProperties.getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            // S3-compatible providers (e.g. Oracle Object Storage) use path-style URLs
            return endpoint.replaceAll("/+$", "") + "/" + bucket + "/" + key;
        }
        String region = awsProperties.getRegion();
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String extractKey(String url) {
        // Strip everything up to and including "/<bucket>/" so it works for both
        // path-style (Oracle/MinIO) and virtual-host (AWS) URLs.
        try {
            String marker = "/" + awsProperties.getS3().getBucket() + "/";
            int idx = url.indexOf(marker);
            if (idx >= 0) {
                return url.substring(idx + marker.length());
            }
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
