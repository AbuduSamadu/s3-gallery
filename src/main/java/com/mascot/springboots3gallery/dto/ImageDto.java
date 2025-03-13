package com.mascot.springboots3gallery.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {

    private String key;          // Unique identifier for the image in S3
    private String url;          // Pre-signed URL for accessing the image
    private String contentType;  // MIME type of the image (e.g., image/jpeg)
    private long size;           // Size of the image in bytes
}