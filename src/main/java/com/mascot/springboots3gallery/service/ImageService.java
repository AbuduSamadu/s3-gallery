package com.mascot.springboots3gallery.service;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
public class ImageService {
    private final S3Service s3Service;
    Logger logger = LoggerFactory.getLogger(ImageService.class);

    public ImageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }


    // Upload an image
    public void uploadImage(MultipartFile file, String imageName) throws IOException {
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            logger.error("Only image files are allowed.");
            throw new BadRequestException("Only image files are allowed.");
        }

        File tempFile = convertMultipartFileToFile(file);
        s3Service.uploadFile(imageName, tempFile);
        s3Service.generatePresidedUrl(imageName);
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