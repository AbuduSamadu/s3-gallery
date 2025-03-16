package com.mascot.springboots3gallery.dto;

import java.time.LocalDateTime;

public class ImageDto {
    private String key; // Unique identifier (e.g., file name) in S3
    private String url; // Pre-signed URL for accessing the image
    private String name; // User-provided image name (derived from the key)
    private String contentType; // MIME type of the image (e.g., "image/jpeg")
    private Long size; // Size of the image in bytes
    private LocalDateTime uploadedAt; // Timestamp when the image was uploaded

    // Constructor
    public ImageDto(String key, String url, String contentType, Long size, String name, LocalDateTime uploadedAt) {
        this.key = key;
        this.url = url;
        this.contentType = contentType;
        this.size = size;
        this.name = extractNameFromKey(key); // Extract name from the key
        this.uploadedAt = LocalDateTime.now(); // Default to current time
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
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
        return fileName; // Return the full name if no extension is found
    }
}