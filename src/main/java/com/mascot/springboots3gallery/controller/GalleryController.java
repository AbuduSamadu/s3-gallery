package com.mascot.springboots3gallery.controller;

import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.service.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/images")
public class GalleryController {

    private final ImageService imageService;

    public GalleryController(ImageService imageService) {
        this.imageService = imageService;
    }

    // Endpoint for API (JSON response)
    @GetMapping("/gallery")
    public ResponseEntity<Page<ImageDto>> listImagesApi(Pageable pageable) {
        Page<ImageDto> images = imageService.listImages(pageable);
        return ResponseEntity.ok(images);
    }

    // Endpoint for Thymeleaf (HTML response)
    @GetMapping
    public String getGalleryPage(Model model, Pageable pageable) {
        Page<ImageDto> images = imageService.listImages(pageable);
        model.addAttribute("content", images.getContent());
        model.addAttribute("pageNumber", images.getNumber());
        model.addAttribute("pageSize", images.getSize());
        model.addAttribute("totalElements", images.getTotalElements());
        model.addAttribute("totalPages", images.getTotalPages());
        model.addAttribute("last", images.isLast());
        return "index"; // Name of the Thymeleaf template
    }
}