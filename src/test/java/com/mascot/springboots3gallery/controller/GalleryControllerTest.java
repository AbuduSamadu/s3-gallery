package com.mascot.springboots3gallery.controller;

import com.mascot.springboots3gallery.dto.ImageDto;
import com.mascot.springboots3gallery.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GalleryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ImageService imageService;

    @Mock
    private Model model;

    @InjectMocks
    private GalleryController galleryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(galleryController).build();
    }

    @Test
    void listImagesApi_Success() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        ImageDto image1 = new ImageDto("3", "image1.jpg", "https://example.com/image1.jpg", 23L, "kwame", LocalDateTime.now());
        ImageDto image2 = new ImageDto("4", "image2.jpg", "https://example.com/image2.jpg", 45L, "kwame", LocalDateTime.now());
        Page<ImageDto> mockPage = new PageImpl<>(Arrays.asList(image1, image2), pageable, 2);

        when(imageService.listImages(any(Pageable.class))).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/api/images/gallery")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("image1.jpg"))
                .andExpect(jsonPath("$.content[0].url").value("https://example.com/image1.jpg"))
                .andExpect(jsonPath("$.content[1].id").value("2"))
                .andExpect(jsonPath("$.content[1].name").value("image2.jpg"))
                .andExpect(jsonPath("$.content[1].url").value("https://example.com/image2.jpg"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(imageService, times(1)).listImages(any(Pageable.class));
    }

    @Test
    void getGalleryPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        ImageDto image1 = new ImageDto("1", "image1.jpg", "https://example.com/image1.jpg", 23L, "kwame", LocalDateTime.now());
        ImageDto image2 = new ImageDto("2", "image2.jpg", "https://example.com/image2.jpg", 45L, "kwame", LocalDateTime.now());
        Page<ImageDto> mockPage = new PageImpl<>(Arrays.asList(image1, image2), pageable, 2);

        when(imageService.listImages(any(Pageable.class))).thenReturn(mockPage);

        // Act
        String viewName = galleryController.getGalleryPage(model, pageable);

        // Assert
        verify(imageService, times(1)).listImages(any(Pageable.class));
        verify(model, times(1)).addAttribute(eq("content"), eq(mockPage.getContent()));
        verify(model, times(1)).addAttribute(eq("pageNumber"), eq(mockPage.getNumber()));
        verify(model, times(1)).addAttribute(eq("pageSize"), eq(mockPage.getSize()));
        verify(model, times(1)).addAttribute(eq("totalElements"), eq(mockPage.getTotalElements()));
        verify(model, times(1)).addAttribute(eq("totalPages"), eq(mockPage.getTotalPages()));
        verify(model, times(1)).addAttribute(eq("last"), eq(mockPage.isLast()));

        assert viewName.equals("index"); // Verify the view name is "index"
    }
}