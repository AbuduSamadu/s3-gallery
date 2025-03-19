package com.mascot.springboots3gallery.service;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.exception.InternalServerErrorException;
import com.mascot.springboots3gallery.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class S3Service {

    private static final String BUCKET_NAME = "image-gallery-mascot";

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ContentTypeUtil contentTypeUtil;
    private final Map<String, String> imageNameMap = new HashMap<>();

    public S3Service(S3Client s3Client, S3Presigner s3Presigner, ContentTypeUtil contentTypeUtil) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.contentTypeUtil = contentTypeUtil;
    }

    // Upload a file to S3
    public void uploadFile(String key, File file) {
        PutObjectRequest request = PutObjectRequest
                .builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .contentLength(file.length())
                .build();
        s3Client.putObject(request, RequestBody.fromFile(file));
    }

    // Check if a file exists in S3
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest
                    .builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    public void updateImageName(String key, String newName) {
        if (!imageNameMap.containsKey(key)) {
            throw new ResourceNotFoundException("Image with key " + key + " not found.");
        }
        imageNameMap.put(key, newName);
        logger.info("Updated image name for key {}: {}", key, newName);
    }


    // Generate a pre-signed URL for accessing an image
    public String generatePresidedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(BUCKET_NAME).key(key).build();

        PresignedGetObjectRequest resignedRequest = s3Presigner
                .presignGetObject(r -> r
                        .getObjectRequest(getObjectRequest)
                        .signatureDuration(Duration.ofMinutes(120)));

        return resignedRequest.url().toString();
    }


    // List images from S3 with pagination
    public Page<ImageDto> listImages(Pageable pageable) {
        // Step 1: Fetch all objects in the bucket to calculate totalElements
        List<S3Object> allObjects = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Response response = s3Client.
                    listObjectsV2(ListObjectsV2Request
                            .builder()
                            .bucket(BUCKET_NAME)
                            .maxKeys(1000) // Fetch up to 1000 objects per request (S3 limit)
                            .continuationToken(continuationToken).build());

            allObjects.addAll(response.contents());
            continuationToken = response.nextContinuationToken();
        } while (continuationToken != null);

        allObjects.removeIf(s3Object -> s3Object.key().endsWith(".zip"));

        int totalElements = allObjects.size(); // Total number of objects in the bucket

        // Step 2: Paginate the objects based on the requested page and pageSize
        int startIndex = (int) pageable.getOffset(); // Start index for the current page
        int endIndex = Math.min(startIndex + pageable.getPageSize(), totalElements); // End index for the current page

        List<S3Object> paginatedObjects = allObjects.subList(startIndex, endIndex);

        // Step 3: Map S3 objects to ImageDto objects
        List<ImageDto> imageDtos = paginatedObjects.stream().map(s3Object -> {
            String key = s3Object.key();
            String presignedUrl = generatePresidedUrl(key);
            String contentType = null;
            LocalDateTime uploadedAt = null;

            try {
                HeadObjectResponse metadata = s3Client
                        .headObject(HeadObjectRequest
                                .builder()
                                .bucket(BUCKET_NAME)
                                .key(key)
                                .build());
                contentType = metadata.contentType();
                uploadedAt = metadata.lastModified().atZone(java.time.ZoneOffset.UTC).toLocalDateTime();
            } catch (Exception e) {
                logger.error("Failed to retrieve metadata for object with key: {}", key);
            }

            if (contentType == null || contentType.equals("application/octet-stream")) {
                contentType = contentTypeUtil.getContentTypeFromKey(key);
            }

            return new ImageDto(key,
                    presignedUrl,
                    contentType,
                    s3Object.size(), extractNameFromKey(key), uploadedAt != null ? uploadedAt : LocalDateTime.now());
        }).toList();

        // Step 4: Calculate totalPages
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        // Log for debugging purposes
        logger.info("Listing images with page {} and size {}. Total elements: {}, Total pages: {}", pageable.getPageNumber(), pageable.getPageSize(), totalElements, totalPages);

        // Step 5: Return paginated results
        return new PageImpl<>(imageDtos, pageable, totalElements);
    }

    // Helper method to extract the image name from the key and remove the file extension
    private String extractNameFromKey(String key) {
        if (key == null || key.isEmpty()) {
            return "Untitled";
        }
        int lastIndex = key.lastIndexOf('/');
        String fileName = (lastIndex != -1) ? key.substring(lastIndex + 1) : key;

        // Remove the file extension
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName; // Return the full name if no extension is found
    }

    public void deleteImage(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(key).build();
            s3Client.deleteObject(request);
            logger.info("Deleted image with key: {}", key);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to delete image with key: ");
        }
    }
}