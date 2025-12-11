package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.GoogleInfoResponseDTO;
import com.moongeul.backend.api.member.dto.GoogleTokenResponseDTO;
import com.moongeul.backend.api.member.dto.UserInfoDTO;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.Role;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.dto.LoginResponseDTO;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.config.jwt.JwtTokenProvider;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthService oAuthService;

    // 인가코드 받아 JWT로 교환 및 회원가입/로그인 처리
    @Transactional
    public LoginResponseDTO loginWithGoogle(String code){

        // 1. 인가 코드로 Google Access Token 및 사용자 정보 획득
        GoogleTokenResponseDTO tokenResponse = oAuthService.getGoogleToken(code);
        GoogleInfoResponseDTO userInfo = oAuthService.getGoogleUserInfo(tokenResponse.getAccessToken());

        // 2. 사용자 정보 추출
        String socialId = userInfo.getId();
        String email = userInfo.getEmail();
        String name = userInfo.getName();
        String picture = userInfo.getPicture();

        // 3. DB 처리 (회원가입 또는 로그인)
        Member member = memberRepository.findBySocialId(socialId)
                .map(entity -> entity.update(name, picture)) // 이미 있으면 정보 업데이트
                .orElseGet(() -> signUp(socialId, email, name, picture)); // 없으면 신규 회원가입

        // 4. 자체 JWT 토큰 생성 및 반환
        JwtTokenDTO jwtToken = jwtTokenProvider.generateToken(member);
        member.updateRefreshToken(jwtToken.getRefreshToken()); // 생성된 refreshToken DB 저장

        return LoginResponseDTO.builder()
                .role(member.getAuthorityKey())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }

    // 신규 회원가입 처리 로직 (DB 저장)
    private Member signUp(String socialId, String email, String name, String picture) {
        Member newUser = Member.builder()
                .email(email)
                .name(name)
                .profileImage(picture)
                .password("OAuth Password") // 임시 패스워드
                .socialId(socialId) // 예시 사용자명 생성
                .socialType("google")
                .role(Role.GUEST) // 이후 필요 정보 모두 입력 시 USER 로 승격
                .build();
        return memberRepository.save(newUser);
    }

    // 사용자 정보 조회
    @Transactional(readOnly = true)
    public UserInfoDTO getUserInfo(String email){

        Member member = getMemberByEmail(email);

        return UserInfoDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .nickname(member.getNickname())
                .build();
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
