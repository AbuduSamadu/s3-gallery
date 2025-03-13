package com.mascot.springboots3gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponseDto<T> {

    private List<T> content;     // List of items (e.g., images)
    private int pageNumber;      // Current page number
    private int pageSize;        // Number of items per page
    private long totalElements;  // Total number of items
    private int totalPages;      // Total number of pages
    private boolean last;        // Indicates if this is the last page
}