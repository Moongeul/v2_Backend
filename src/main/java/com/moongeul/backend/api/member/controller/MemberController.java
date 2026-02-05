package com.moongeul.backend.api.member.controller;

import com.moongeul.backend.api.member.dto.*;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.service.FollowService;
import com.moongeul.backend.api.member.service.MemberService;
import com.moongeul.backend.api.post.dto.CategoryPostListResponseDTO;
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
            description = "토큰을 통해 인증된 사용자의 정보를 반환합니다. userId 쿼리 파라미터가 없으면 본인 정보를 조회하고, 있으면 해당 사용자의 정보를 조회합니다." +
                    "<br><br>[enum]독서 취향 유형 ->" +
                    "<br>- EMOTIONAL_REFLECTOR: 감성 사색 정리러" +
                    "<br>- CHATTY_READER: 수다쟁이 책러" +
                    "<br>- TREND_HUNTER: 신상 헌터" +
                    "<br>- SYSTEMATIC_READER: 정리왕 서평러" +
                    "<br>- IMMERSIVE_READER: 넷플릭스급 몰입러" +
                    "<br>- SECRET_DIARIST: 비밀 일기장 주인" +
                    "<br>- GENRE_SPECIALIST: 장르 고인물" +
                    "<br>- RANDOM_PICKER: 랜덤 피커" +
                    "<br><br>[enum]myFollowStatus ->" +
                    "<br>- NONE: 팔로우 아님" +
                    "<br>- PENDING: 요청 대기중" +
                    "<br>- ACCEPTED: 팔로우 완료"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId){
        UserInfoDTO response = memberService.getUserInfo(userDetails.getUsername(), userId);
        return ApiResponse.success(SuccessStatus.GET_USERINFO_SUCCESS, response);
    }

    @Operation(
            summary = "기록 통계 조회 API (마이페이지 기록장)",
            description = "사용자의 기록 작성 통계를 조회합니다. userId 쿼리 파라미터가 없으면 본인 정보를 조회하고, 있으면 해당 사용자의 정보를 조회합니다. " +
                    "전체 작성 갯수와 카테고리별 기록 갯수, 카테고리 이름, 카테고리 ID를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 통계 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/post-stats")
    public ResponseEntity<ApiResponse<PostStatsResponseDTO>> getPostStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId){
        PostStatsResponseDTO response = memberService.getPostStats(userDetails.getUsername(), userId);
        return ApiResponse.success(SuccessStatus.GET_POST_STATS_SUCCESS, response);
    }

    @Operation(
            summary = "카테고리별 기록 리스트 조회 API (마이페이지 기록장 상세)",
            description = "특정 카테고리에 작성된 기록들을 조회합니다. userId 쿼리 파라미터가 없으면 본인, 있으면 해당 사용자의 기록을 조회합니다. " +
                    "최신순, 오래된순, 평점 높은순, 평점 낮은순으로 정렬할 수 있습니다." +
                    "<br><br>[enum] 정렬 옵션 (sortBy):" +
                    "<br>- LATEST: 최신순 (기본값)" +
                    "<br>- OLDEST: 오래된순" +
                    "<br>- RATING_HIGH: 평점 높은순" +
                    "<br>- RATING_LOW: 평점 낮은순"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카테고리별 기록 리스트 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 카테고리를 찾을 수 없습니다.")
    })
    @GetMapping("/post-stats/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryPostListResponseDTO>> getCategoryPostList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "LATEST") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        CategoryPostListResponseDTO categoryPostListResponseDTO =
                memberService.getCategoryPostList(userDetails.getUsername(), userId, categoryId, sortBy, page, size);
        return ApiResponse.success(SuccessStatus.GET_CATEGORY_POST_LIST_SUCCESS, categoryPostListResponseDTO);
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
            summary = "팔로우 API",
            description = "사용자를 팔로우 합니다."
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
            summary = "언팔로우(팔로우 취소) API",
            description = "사용자를 언팔로우 합니다." +
                    "<br>- '승인 대기중'일 때 해당 API 사용 시, 팔로우 요청 취소 됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "언팔로우 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @PostMapping("/unfollow/{id}")
    public ResponseEntity<ApiResponse<Void>> unfollow(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long id){
        followService.unfollow(id, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.UNFOLLOW_SUCCESS);
    }

    @Operation(
            summary = "팔로잉 사용자 목록 조회 API",
            description = "내가 팔로우한 사용자(팔로잉)의 목록을 조회합니다." +
                    "<br><br>[enum] myFollowStatus: 내가 해당 팔로워를 팔로우했는지 확인하는 필드:" +
                    "<br>- NONE: 팔로우 아님" +
                    "<br>- PENDING: 요청 대기중" +
                    "<br>- ACCEPTED: 팔로우 완료" +
                    "<br>- *참고: 해당 목록은 모두 ACCEPTED 입니다. (팔로우한 사용자들이기 때문에)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FollowResponseDTO>>> getFollowings(@AuthenticationPrincipal UserDetails userDetails){
        List<FollowResponseDTO> response = followService.getFollowing(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_FOLLOWING_SUCCESS, response);
    }

    @Operation(
            summary = "팔로워 사용자 목록 조회 API",
            description = "나를 팔로잉한 사용자(팔로워)의 목록을 조회합니다." +
                    "<br><br>[enum] myFollowStatus: 내가 해당 팔로워를 팔로우했는지 확인하는 필드:" +
                    "<br>- NONE: 팔로우 아님" +
                    "<br>- PENDING: 요청 대기중" +
                    "<br>- ACCEPTED: 팔로우 완료"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/follower")
    public ResponseEntity<ApiResponse<List<FollowResponseDTO>>> getFollowers(@AuthenticationPrincipal UserDetails userDetails){
        List<FollowResponseDTO> response = followService.getFollower(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_FOLLOWER_SUCCESS, response);
    }

    /*
     *
     * 닉네임 API
     *
     * */
    @Operation(
            summary = "랜덤 닉네임 재생성 API",
            description = "랜덤 닉네임을 다시 생성하여 등록하고 바뀐 닉네임을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 재생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @PostMapping("/nickname/regenerate")
    public ResponseEntity<ApiResponse<NicknameResponseDTO>> regenerateNickname(@AuthenticationPrincipal UserDetails userDetails) {

        NicknameResponseDTO nicknameResponseDTO = memberService.regenerateNickname(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.REGENERATE_NICKNAME_SUCCESS, nicknameResponseDTO);
    }

    @Operation(
            summary = "닉네임 등록 API",
            description = "사용자가 직접 입력한 닉네임을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 사용 중인 닉네임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<NicknameResponseDTO>> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NicknameRequestDTO nicknameRequestDTO) {
        
        NicknameResponseDTO nicknameResponseDTO = memberService.updateNickname(userDetails.getUsername(), nicknameRequestDTO);
        return ApiResponse.success(SuccessStatus.UPDATE_NICKNAME_SUCCESS, nicknameResponseDTO);
    }

    @Operation(
            summary = "닉네임 중복 체크 API",
            description = "전달받은 닉네임이 중복인지 체크합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 중복 체크 성공")
    })
    @GetMapping("/nickname/check")
    public ResponseEntity<ApiResponse<NicknameCheckResponseDTO>> checkNicknameDuplicate(
            @RequestParam String nickname) {
        
        NicknameCheckResponseDTO nicknameCheckResponseDTO = memberService.checkNicknameDuplicate(nickname);
        return ApiResponse.success(SuccessStatus.CHECK_NICKNAME_DUPLICATE_SUCCESS, nicknameCheckResponseDTO);
    }
    
}
