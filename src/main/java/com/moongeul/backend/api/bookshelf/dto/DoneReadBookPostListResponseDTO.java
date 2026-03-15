package com.moongeul.backend.api.bookshelf.dto;

import com.moongeul.backend.api.post.dto.PostDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoneReadBookPostListResponseDTO {

    private String title;
    private String isbn;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private boolean isLast;
    private List<PostDTO> data;
}
