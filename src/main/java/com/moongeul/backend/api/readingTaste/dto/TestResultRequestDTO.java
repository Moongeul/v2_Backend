package com.moongeul.backend.api.readingTaste.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultRequestDTO {

    private String guestUuid;
    private ReadingTasteType readingTasteType;
}
