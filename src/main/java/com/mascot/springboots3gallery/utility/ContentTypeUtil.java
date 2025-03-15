package com.mascot.springboots3gallery.utility;

import org.springframework.stereotype.Component;

@Component
public class ContentTypeUtil {
    public String getContentTypeFromKey(String key) {
        if (key == null || key.isEmpty()) {
            return "application/octet-stream"; // Default fallback for invalid keys
        }

        // Extract the file extension
        int lastDotIndex = key.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "application/octet-stream"; // No file extension, use default
        }

        String extension = key.substring(lastDotIndex + 1).toLowerCase();

        // Map extensions to MIME types
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "svg":
                return "image/svg+xml";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream"; // Default fallback
        }
    }

}
