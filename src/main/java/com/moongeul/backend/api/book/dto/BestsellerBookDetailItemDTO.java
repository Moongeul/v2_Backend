package com.moongeul.backend.api.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestsellerBookDetailItemDTO {

    private String isbn;
    private String title;
    private String bookImage;
    private String author;
    private String publisher;
    private String pubdate;
    private Double ratingAverage;
}
