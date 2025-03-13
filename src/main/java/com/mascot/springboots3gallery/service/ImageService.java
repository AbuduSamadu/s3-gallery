package com.mascot.springboots3gallery.service;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.exception.BadRequestException;
import com.mascot.springboots3gallery.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ImageService {

    @Autowired
    private S3Service s3Service;

    // Upload an image
    public String uploadImage(MultipartFile file) throws IOException {
        if (!file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed.");
        }
        File tempFile = convertMultipartFileToFile(file);
        String key = file.getOriginalFilename();
        s3Service.uploadFile("your-bucket-name", key, tempFile);
        return s3Service.generatePresignedUrl("your-bucket-name", key);
    }

    // Retrieve an image by key
    public String getImage(String key) {
        if (!s3Service.fileExists("your-bucket-name", key)) {
            throw new ResourceNotFoundException("Image not found.");
        }
        return s3Service.generatePresignedUrl("your-bucket-name", key);
    }

    // List images with pagination
    public Page<ImageDto> listImages(Pageable pageable) {
        return s3Service.listImages(pageable);
    }

    // Convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);
        return tempFile;
    }
}