package com.moongeul.backend.api.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleInfoResponseDTO {

    private String id;
    private String email;
    private String name;
    private String picture;
}
