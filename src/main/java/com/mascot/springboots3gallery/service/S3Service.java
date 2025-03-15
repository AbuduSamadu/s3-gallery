package com.mascot.springboots3gallery.service;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.utility.ContentTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.time.Duration;
import java.util.List;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ContentTypeUtil contentTypeUtil;
    private static final String BUCKET_NAME = "image-gallery-mascot";

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    public S3Service(S3Client s3Client, S3Presigner s3Presigner, ContentTypeUtil contentTypeUtil) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.contentTypeUtil = contentTypeUtil;
    }
    // Upload a file to S3
    public void uploadFile(String key, File file) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromFile(file));
    }

    // Check if a file exists in S3
    public boolean fileExists( String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    // Generate a pre-signed URL for accessing an image
    public String generatePresidedUrl( String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();

        PresignedGetObjectRequest resignedRequest = s3Presigner.presignGetObject(r -> r
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(120)));

        return resignedRequest.url().toString();
    }

    // List images from S3 with pagination
    public Page<ImageDto> listImages(Pageable pageable) {
        ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .maxKeys(pageable.getPageSize())
                .startAfter(pageable.getPageNumber() > 0 ? String.valueOf(pageable.getPageNumber()) : null)
                .build());

        List<S3Object> objects = response.contents();
        List<ImageDto> imageDtos = objects.stream()
                .map(s3Object -> {
                    String key = s3Object.key();
                    logger.info("Processing object with key: {}", key);

                    // Generate pre-signed URL
                    String presignedUrl = generatePresidedUrl(key);
                    logger.info("Generated pre-signed URL for {}: {}", key, presignedUrl);

                    // Retrieve metadata (content type)
                    String contentType = null;
                    try {
                        HeadObjectResponse metadata = s3Client.headObject(HeadObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(key)
                                .build());
                        contentType = metadata.contentType();
                    } catch (Exception e) {
                        logger.warn("Failed to retrieve metadata for {}: {}", key, e.getMessage());
                    }

                    // Fallback to inferring content type from key
                    if (contentType == null || contentType.equals("application/octet-stream")) {
                        contentType = contentTypeUtil.getContentTypeFromKey(key);
                        logger.info("Inferred content type for {}: {}", key, contentType);
                    }

                    return new ImageDto(
                            key,
                            presignedUrl,
                            contentType,
                            s3Object.size());
                })
                .toList();

        logger.info("Returning {} images for page {} with size {}", imageDtos.size(), pageable.getPageNumber(), pageable.getPageSize());
        return new PageImpl<>(imageDtos, pageable, response.keyCount());
    }
}