package com.moongeul.backend.api.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplePublicKeysResponseDTO {

    private List<ApplePublicKey> keys;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApplePublicKey {
        private String kid;
        private String alg;
        private String n;
        private String e;
    }
}
