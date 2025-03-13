package com.mascot.springboots3gallery.service;


import com.mascot.springboots3gallery.dto.ImageDto;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    private static final String BUCKET_NAME = "your-bucket-name";

    // Upload a file to S3
    public void uploadFile(String bucketName, String key, File file) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromFile(file));
    }

    // Check if a file exists in S3
    public boolean fileExists(String bucketName, String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    // Generate a pre-signed URL for accessing an image
    public String generatePresignedUrl(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(r -> r
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(10)));

        return presignedRequest.url().toString();
    }

    // List images from S3 with pagination
    public Page<ImageDto> listImages(Pageable pageable) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .maxKeys((int) pageable.getPageSize())
                .startAfter(pageable.getPageNumber() > 0 ? String.valueOf(pageable.getPageNumber()) : null)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        List<S3Object> objects = response.contents();

        List<ImageDto> imageDtos = objects.stream()
                .map(s3Object -> new ImageDto(
                        s3Object.key(),
                        generatePresignedUrl(BUCKET_NAME, s3Object.key()),
                        "image/jpeg", // Replace with actual content type if needed
                        s3Object.size()))
                .collect(Collectors.toList());

        return new PageImpl<>(imageDtos, pageable, response.keyCount());
    }
}