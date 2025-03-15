package com.mascot.springboots3gallery.service;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.exception.BadRequestException;
import com.mascot.springboots3gallery.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ImageService {
    Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final S3Service s3Service;

    public ImageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }


    // Upload an image
    public String uploadImage(MultipartFile file) throws IOException {
        if (!file.getContentType().startsWith("image/")) {
            logger.error("Only image files are allowed.");
            throw new BadRequestException("Only image files are allowed.");
        }
        File tempFile = convertMultipartFileToFile(file);
        String key = file.getOriginalFilename();
        s3Service.uploadFile(key, tempFile);
        return s3Service.generatePresidedUrl( key);
    }

    // Retrieve an image by key
    public String getImage(String key) {
        if (!s3Service.fileExists( key)) {
            logger.error("Image not found.");
            throw new ResourceNotFoundException("Image not found.");
        }
        logger.info("Retrieving image with key: {}", key);
        return s3Service.generatePresidedUrl( key);
    }

    // List images with pagination
    public Page<ImageDto> listImages(Pageable pageable) {
        logger.info("Listing images with page {} and size {}", pageable.getPageNumber(), pageable.getPageSize());
        return s3Service.listImages(pageable);
    }

    // Convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);
        return tempFile;
    }
}