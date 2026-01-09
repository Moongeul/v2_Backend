package com.moongeul.backend.api.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleInfoResponseDTO {

    @JsonProperty("sub")
    private String id;
    private String email;
    private String name;
    private String picture;
}
