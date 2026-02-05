package com.moongeul.backend.api.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequestDTO {

    private String token;
    private String platform; // AND, IOS, WEB
}
