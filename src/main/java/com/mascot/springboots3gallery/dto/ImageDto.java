package com.mascot.springboots3gallery.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {

    private String key;
    private String url;
    private String contentType;
    private long size;
}