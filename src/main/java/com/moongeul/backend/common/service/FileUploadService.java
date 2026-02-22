package com.moongeul.backend.common.service;

import com.moongeul.backend.common.config.minio.MinioProperties;
import com.moongeul.backend.common.exception.InternalServerException;
import com.moongeul.backend.common.response.ErrorStatus;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String uploadFile(MultipartFile file, String objectPrefix) {
        ensureBucketExists();

        String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
        String objectName = createObjectName(objectPrefix, extension);
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("파일 업로드 실패 - objectName: {}", objectName, e);
            throw new InternalServerException(ErrorStatus.FILE_UPLOAD_FAIL.getMessage());
        }

        return buildPublicUrl(objectName);
    }

    public void deleteFileByUrl(String fileUrl) {
        Optional<String> objectName = extractObjectName(fileUrl);
        if (objectName.isEmpty()) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName.get())
                            .build()
            );
        } catch (Exception e) {
            log.error("파일 삭제 실패 - objectName: {}", objectName.get(), e);
            throw new InternalServerException(ErrorStatus.FILE_DELETE_FAIL.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Minio 버킷 확인/생성 실패 - bucket: {}", minioProperties.getBucket(), e);
            throw new InternalServerException(ErrorStatus.INTERNAL_SERVER_EXCEPTION.getMessage());
        }
    }

    private String createObjectName(String objectPrefix, String extension) {
        LocalDate today = LocalDate.now();
        String prefix = normalizePrefix(objectPrefix);

        return String.format(
                "%s/%d/%02d/%s.%s",
                prefix,
                today.getYear(),
                today.getMonthValue(),
                UUID.randomUUID(),
                extension
        );
    }

    private String normalizePrefix(String objectPrefix) {
        if (!StringUtils.hasText(objectPrefix)) {
            return "common";
        }

        String trimmed = objectPrefix.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return StringUtils.hasText(trimmed) ? trimmed : "common";
    }

    private String resolveExtension(String originalFilename, String contentType) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (StringUtils.hasText(extension)) {
            return extension.toLowerCase();
        }

        if (!StringUtils.hasText(contentType)) {
            return "bin";
        }

        if (contentType.contains("/")) {
            return contentType.substring(contentType.indexOf('/') + 1).toLowerCase();
        }

        return "bin";
    }

    private String buildPublicUrl(String objectName) {
        String endpoint = trimTrailingSlash(minioProperties.getPublicEndpoint());
        return endpoint + "/" + minioProperties.getBucket() + "/" + objectName;
    }

    private Optional<String> extractObjectName(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return Optional.empty();
        }

        try {
            URI uri = URI.create(fileUrl);
            String path = uri.getPath();
            String prefix = "/" + minioProperties.getBucket() + "/";

            if (!StringUtils.hasText(path) || !path.startsWith(prefix)) {
                return Optional.empty();
            }

            return Optional.of(path.substring(prefix.length()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String trimTrailingSlash(String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            return "";
        }
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }
}
