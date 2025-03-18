package com.mascot.springboots3gallery.controller;


import com.mascot.springboots3gallery.dto.UploadResponseDto;
import com.mascot.springboots3gallery.service.ImageService;
import com.mascot.springboots3gallery.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;
    private final ImageService imageService;

    public ImageController(S3Service s3Service, ImageService imageService) {
        this.s3Service = s3Service;
        this.imageService = imageService;
    }


    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("imageName") String imageName) throws IOException {
        imageService.uploadImage(file, imageName);
        String imageUrl = s3Service.generatePresidedUrl(file.getOriginalFilename());
        return ResponseEntity.ok(new UploadResponseDto("File uploaded successfully.", imageUrl));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteImage(@PathVariable String key) {
        s3Service.deleteImage(key);
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> updateImageName(@PathVariable String key, @RequestBody Map<String, String> requestBody) {
        String newName = requestBody.get("name");
        if (newName == null || newName.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        s3Service.updateImageName(key, newName);
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }

}