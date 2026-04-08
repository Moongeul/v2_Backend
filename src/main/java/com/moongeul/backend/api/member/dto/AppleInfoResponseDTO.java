package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppleInfoResponseDTO {

    private String id;
    private String email;
    private String name;
}
