package com.moongeul.backend.api.member.controller;

import com.moongeul.backend.api.member.dto.LoginResponseDTO;
import com.moongeul.backend.api.member.dto.LoginRequestDTO;
import com.moongeul.backend.api.member.dto.UserInfoDTO;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.service.FollowService;
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

import java.util.List;

@Tag(name = "Member", description = "Member(회원) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/member")
public class MemberController {

    private final MemberService memberService;
    private final FollowService followService;

    /*
     *
     * 로그인 API
     *
     * */
    @Operation(
            summary = "구글 로그인 API",
            description = "구글 인가코드을 통해 사용자의 정보를 등록 및 토큰 + 역할을 발급합니다. " +
                    "<br><br>[enum]ROLE -> 처음사용자 : GUEST, 일반사용자 : USER, 관리자 : ADMIN"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인가코드가 입력되지 않았습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 인가코드 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "로그인 서버 오류 발생")
    })
    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginWithGoogle(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        LoginResponseDTO response = memberService.loginWithGoogle(loginRequestDTO.getCode());
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, response);
    }

    @Operation(
            summary = "카카오 로그인 API",
            description = "카카오 인가코드을 통해 사용자의 정보를 등록 및 토큰 + 역할을 발급합니다. (ROLE -> 처음사용자 : GUEST, 일반사용자 : USER, 관리자 : ADMIN)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인가코드가 입력되지 않았습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 인가코드 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "로그인 서버 오류 발생")
    })
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginWithKakao(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        LoginResponseDTO response = memberService.loginWithKakao(loginRequestDTO.getCode());
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, response);
    }

    @Operation(
            summary = "사용자 정보 조회 API",
            description = "토큰을 통해 인증된 사용자의 정보를 반환합니다." +
                    "<br><br>[enum]독서 취향 유형 ->" +
                    "<br>- EMOTIONAL_REFLECTOR: 감성 사색 정리러" +
                    "<br>- CHATTY_READER: 수다쟁이 책러" +
                    "<br>- TREND_HUNTER: 신상 헌터" +
                    "<br>- SYSTEMATIC_READER: 정리왕 서평러" +
                    "<br>- IMMERSIVE_READER: 넷플릭스급 몰입러" +
                    "<br>- SECRET_DIARIST: 비밀 일기장 주인" +
                    "<br>- GENRE_SPECIALIST: 장르 고인물" +
                    "<br>- RANDOM_PICKER: 랜덤 피커"
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

    @Operation(
            summary = "토큰 재발급 API",
            description = "엑세스 토큰 만료 시, 유효한 리프레시 토큰을 통해 엑세스 토큰을 재발급 받습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰이 만료되었거나 유효하지 않은 토큰입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/reissue-token")
    public ResponseEntity<ApiResponse<JwtTokenDTO>> reissueAccessToken(@RequestHeader(value = "Authorization-Refresh") String refreshToken){
        JwtTokenDTO response = memberService.reissueToken(refreshToken);
        return ApiResponse.success(SuccessStatus.REISSUE_TOKEN_SUCCESS, response);
    }

    /*
    *
    * 팔로잉/팔로우 API
    *
    * */
    @Operation(
            summary = "팔로우/언팔로우 API",
            description = "사용자를 팔로우 또는 언팔로우 합니다." +
                    "<br>- 팔로우하고 있는 사용자에게 해당 API를 한 번 더 사용 시, 팔로우가 취소됩니다." +
                    "<br>- 즉, 팔로우/언팔로우 두 버튼에 모두 해당 API를 사용하시면 됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로우 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @PostMapping("/follow/{id}")
    public ResponseEntity<ApiResponse<Void>> follow(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable Long id){
        followService.follow(id, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.FOLLOW_SUCCESS);
    }

    @Operation(
            summary = "팔로잉 사용자 목록 조회 API",
            description = "내가 팔로우한 사용자(팔로잉)의 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<UserInfoDTO>>> getFollowings(@AuthenticationPrincipal UserDetails userDetails){
        List<UserInfoDTO> response = followService.getfollowing(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_FOLLOWING_SUCCESS, response);
    }

    @Operation(
            summary = "팔로워 사용자 목록 조회 API",
            description = "나를 팔로잉한 사용자(팔로워)의 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/follower")
    public ResponseEntity<ApiResponse<List<UserInfoDTO>>> getFollowers(@AuthenticationPrincipal UserDetails userDetails){
        List<UserInfoDTO> response = followService.getfollower(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_FOLLOWER_SUCCESS, response);
    }
    
}
