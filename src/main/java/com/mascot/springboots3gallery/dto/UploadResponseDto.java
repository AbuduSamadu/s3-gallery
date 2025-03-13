package com.mascot.springboots3gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseDto {

    private String message;      // Success message
    private String imageUrl;     // Pre-signed URL for accessing the uploaded image
}