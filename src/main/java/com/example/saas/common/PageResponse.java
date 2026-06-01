package com.example.saas.common;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private long totalPages;
    private int pageNumber;
    private int pageSize;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private boolean firstPage;
    private boolean lastPage;

    public static <T> PageResponse<T> of(final Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .hasNextPage(page.hasNext())
                .hasPreviousPage(page.hasPrevious())
                .firstPage(page.isFirst())
                .lastPage(page.isLast())
                .build();
    }
}
