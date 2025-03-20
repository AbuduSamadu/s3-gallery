package com.mascot.springboots3gallery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mascot.springboots3gallery.service.ImageService;
import com.mascot.springboots3gallery.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    @Mock
    private S3Service s3Service;
    @Mock
    private ImageService imageService;
    @InjectMocks
    private ImageController imageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
    }

    @Test
    void uploadImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test data".getBytes());
        String imageName = "testImage";
        String imageUrl = "https://example.com/test.jpg";


        when(s3Service.generatePresidedUrl(eq("test.jpg"))).thenReturn(imageUrl);

        // Act & Assert
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .param("imageName", imageName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File uploaded successfully."))
                .andExpect(jsonPath("$.imageUrl").value(imageUrl));

        verify(imageService, times(1)).uploadImage(any(), eq(imageName));
        verify(s3Service, times(1)).generatePresidedUrl(eq("test.jpg"));
    }

    @Test
    void deleteImage_Success() throws Exception {
        // Arrange
        String key = "testKey";

        doNothing().when(s3Service).deleteImage(eq(key));

        // Act & Assert
        mockMvc.perform(delete("/api/images/{key}", key))
                .andExpect(status().isNoContent());

        verify(s3Service, times(1)).deleteImage(eq(key));
    }

    @Test
    void updateImageName_Success() throws Exception {
        // Arrange
        String key = "testKey";
        String newName = "newName";

        doNothing().when(s3Service).updateImageName(eq(key), eq(newName));

        // Act & Assert
        mockMvc.perform(put("/api/images/{key}", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", newName))))
                .andExpect(status().isNoContent());

        verify(s3Service, times(1)).updateImageName(eq(key), eq(newName));
    }

    @Test
    void updateImageName_BadRequest_InvalidName() throws Exception {
        // Arrange
        String key = "testKey";

        // Act & Assert
        mockMvc.perform(put("/api/images/{key}", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", ""))))
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).updateImageName(anyString(), anyString());
    }
}