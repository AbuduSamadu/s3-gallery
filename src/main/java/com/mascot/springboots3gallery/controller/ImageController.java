package com.mascot.springboots3gallery.controller;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.dto.PaginationResponseDto;
import com.mascot.springboots3gallery.dto.UploadResponseDto;
import com.mascot.springboots3gallery.exception.BadRequestException;
import com.mascot.springboots3gallery.service.ImageService;
import com.mascot.springboots3gallery.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;
    private final S3Service s3Service;

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(ImageService imageService, S3Service s3Service) {
        this.imageService = imageService;
        this.s3Service = s3Service;
    }


    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageName") String imageName) throws IOException {

        if (!file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed.");
        }

        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);

        String key = imageName + "-" + file.getOriginalFilename();
        s3Service.uploadFile(key, tempFile);

        String presignedUrl = s3Service.generatePresidedUrl(key);
        return ResponseEntity.ok(new UploadResponseDto("File uploaded successfully.", presignedUrl));
    }

    @GetMapping("/{key}")
    public String getImage(@PathVariable String key) {

        logger.info("Retrieving image with key: {}", imageService.getImage(key));
        return imageService.getImage(key);
    }

    @GetMapping
    public PaginationResponseDto<ImageDto> getImages(Pageable pageable) {

        logger.info("Listing images with page {} and size {}", pageable.getPageNumber(), pageable.getPageSize());
        return new PaginationResponseDto<>(imageService.listImages(pageable));
    }
}