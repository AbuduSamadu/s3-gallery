package com.mascot.springboots3gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponseDto<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;


    public PaginationResponseDto(Page<ImageDto> imageDtos) {
        this.pageNumber = imageDtos.getNumber();
        this.pageSize = imageDtos.getSize();
        this.totalElements = imageDtos.getTotalElements();
        this.totalPages = imageDtos.getTotalPages();
        this.last = imageDtos.isLast();
    }
}