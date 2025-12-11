package com.moongeul.backend.api.member.controller;

import com.moongeul.backend.api.member.dto.LoginResponseDTO;
import com.moongeul.backend.api.member.dto.LoginRequestDTO;
import com.moongeul.backend.api.member.dto.UserInfoDTO;
import com.moongeul.backend.api.member.service.MemberService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "Member(회원) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/member")
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "로그인 API",
            description = "구글 인가코드을 통해 사용자의 정보를 등록 및 토큰 + 역할을 발급합니다. (ROLE -> 처음사용자 : GUEST, 일반사용자 : USER, 관리자 : ADMIN)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "구글 엑세스토큰이 입력되지 않았습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 엑세스토큰 입니다.")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginWithGoogle(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        LoginResponseDTO response = memberService.loginWithGoogle(loginRequestDTO.getCode());
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, response);
    }

    @Operation(
            summary = "사용자 정보 조회 API",
            description = "토큰을 통해 인증된 사용자의 정보를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails){
        UserInfoDTO response = memberService.getUserInfo(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_USERINFO_SUCCESS, response);
    }
    
}
