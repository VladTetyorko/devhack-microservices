package com.vladte.devhack.domain.service.files;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Base service for file storage operations with Minio.
 * <ul>
 *     <li>Template methods for upload, download, delete, and presigned URLs.</li>
 *     <li>Built-in validation, retry with exponential backoff, and metrics hooks.</li>
 *     <li>Extensible generateObjectName strategy for versioning and namespacing.</li>
 * </ul>
 */
public abstract class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final MinioClient minioClient;
    private final String baseUrl;
    private final int maxUploadRetries = 3;

    protected FileStorageService(MinioClient minioClient, String baseUrl) {
        this.minioClient = minioClient;
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
    }

    /**
     * Uploads a file and returns its accessible URL.
     */
    public final String uploadFile(String prefix, MultipartFile file) throws IOException {
        validateFile(file);
        String objectName = generateObjectName(prefix, file);
        retryWithBackoff(() -> {
            putObject(objectName, file);
            return null;
        }, maxUploadRetries);
        return buildFileUrl(objectName);
    }

    /**
     * Downloads a file by its URL.
     */
    public InputStream downloadFile(String fileUrl)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        String objectName = extractObjectName(fileUrl);
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(getBucketName())
                        .object(objectName)
                        .build()
        );
    }

    /**
     * Deletes a file by its URL.
     */
    public void deleteFile(String fileUrl) throws IOException {
        String objectName = extractObjectName(fileUrl);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.warn("Failed to delete object {}: {}", objectName, e.getMessage());
            throw new IOException("Unable to delete file: " + fileUrl, e);
        }
    }

    /**
     * Generates a presigned URL for temporary access.
     */
    public String createPresignedUrl(String fileUrl, Duration expiry)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        String objectName = extractObjectName(fileUrl);
        return minioClient.getPresignedObjectUrl(
                io.minio.GetPresignedObjectUrlArgs.builder()
                        .bucket(getBucketName())
                        .object(objectName)
                        .method(Method.GET)
                        .expiry((int) expiry.getSeconds())
                        .build()
        );
    }

    protected abstract String getBucketName();

    /**
     * Validates basic file properties.
     */
    protected void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("MultipartFile must not be null or empty and must have an original filename");
        }
    }

    /**
     * Builds the final file URL.
     */
    protected String buildFileUrl(String objectName) {
        return String.format("%s/%s/%s", baseUrl, getBucketName(), objectName);
    }

    /**
     * Extracts the object name from a full URL.
     */
    protected String extractObjectName(String fileUrl) {
        String prefix = String.join("/", baseUrl, getBucketName()) + "/";
        if (!fileUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid file URL: " + fileUrl);
        }
        String encoded = fileUrl.substring(prefix.length());
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Default object naming strategy: timestamp + original filename.
     */
    protected String generateObjectName(String prefix, MultipartFile file) {
        String original = file.getOriginalFilename();
        return prefix + "_" + original;
    }

    private void putObject(String objectName, MultipartFile file) throws IOException {
        try (InputStream stream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .stream(stream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.debug("Uploaded object {} to bucket {}", objectName, getBucketName());
        } catch (Exception e) {
            log.error("Error uploading file {}: {}", objectName, e.getMessage());
            throw new IOException("Failed to upload file", e);
        }
    }

    /**
     * Retries a callable with exponential backoff.
     */
    protected <T> T retryWithBackoff(Callable<T> task, int maxAttempts) throws IOException {
        int attempt = 0;
        long backoff = 500L;
        while (true) {
            try {
                return task.call();
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new IOException("Operation failed after " + maxAttempts + " attempts", e);
                }
                log.warn("Attempt {} failed: {}, retrying in {} ms", attempt, e.getMessage(), backoff);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
                backoff *= 2;
            }
        }
    }
}
