package com.mascot.springboots3gallery.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ImageDto {
    // Getters and Setters
    private String key;
    private String url;
    private String name;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;

    // Constructor
    public ImageDto(String key, String url, String contentType, Long size, String name, LocalDateTime uploadedAt) {
        this.key = key;
        this.url = url;
        this.contentType = contentType;
        this.size = size;
        this.name = extractNameFromKey(key);
        this.uploadedAt = LocalDateTime.now();
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
        return fileName;
    }
}