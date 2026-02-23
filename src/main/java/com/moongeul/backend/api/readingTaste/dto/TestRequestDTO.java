package com.moongeul.backend.api.readingTaste.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRequestDTO {

    @Schema(
            description = "비회원 식별을 위한 UUID (브라우저 로컬스토리지 저장용)",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @NotNull(message = "비회원 식별자는 필수입니다")
    private String guestUuid;

    @Schema(
            description = "질문 번호(1-12)와 답변(A or B) 매핑",
            example = "{\"1\":\"A\",\"2\":\"A\",\"3\":\"A\",\"4\":\"A\",\"5\":\"A\",\"6\":\"A\",\"7\":\"A\",\"8\":\"A\",\"9\":\"A\",\"10\":\"A\",\"11\":\"A\",\"12\":\"A\"}"
    )
    @NotNull(message = "테스트 답변 입력은 필수입니다")
    private Map<Integer, String> answers; // {1: "A", 2: "B", ...}
}
