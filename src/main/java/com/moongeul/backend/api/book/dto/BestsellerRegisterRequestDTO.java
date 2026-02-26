package com.moongeul.backend.api.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestsellerRegisterRequestDTO {

    @NotNull(message = "ISBN 리스트는 필수입니다.")
    @Size(min = 1, max = 10, message = "베스트셀러 도서는 1권 이상 10권 이하로 등록해야 합니다.")
    private List<@NotBlank(message = "ISBN은 비어 있을 수 없습니다.") String> isbnList;
}
