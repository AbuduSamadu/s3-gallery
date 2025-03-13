package com.mascot.springboots3gallery.controller;


import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.dto.PaginationResponseDto;
import com.mascot.springboots3gallery.dto.UploadResponseDto;
import com.mascot.springboots3gallery.service.ImageService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public UploadResponseDto uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        String imageUrl = imageService.uploadImage(file);
        return new UploadResponseDto("File uploaded successfully.", imageUrl);
    }

    @GetMapping("/{key}")
    public String getImage(@PathVariable String key) {
        return imageService.getImage(key);
    }

    @GetMapping
    public PaginationResponseDto<ImageDto> getImages(Pageable pageable) {
        return new PaginationResponseDto<>(imageService.listImages(pageable));
    }
}