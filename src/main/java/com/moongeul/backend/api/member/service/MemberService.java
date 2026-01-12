package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.*;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.Role;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.member.util.NicknameGenerator;
import com.moongeul.backend.common.config.jwt.JwtTokenProvider;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final NicknameGenerator nicknameGenerator;

    // 인가코드 받아 JWT로 교환 및 회원가입/로그인 처리
    @Transactional
    public LoginResponseDTO loginWithGoogle(String code){

        // 1. 인가 코드로 Google Access Token 및 사용자 정보 획득
        AccessTokenResponseDTO tokenDTO = googleOAuthService.getGoogleToken(code);
        GoogleInfoResponseDTO userInfo = googleOAuthService.getGoogleUserInfo(tokenDTO.getAccessToken());

        // 2. 사용자 정보 추출
        String socialId = userInfo.getId();
        String email = userInfo.getEmail();
        String name = userInfo.getName();
        String picture = userInfo.getPicture();
        String socialType = "google";

        // 3. DB 처리 (회원가입 또는 로그인)
        Member member = memberRepository.findBySocialId(socialId)
                .map(entity -> entity.update(name, picture)) // 이미 있으면 정보 업데이트
                .orElseGet(() -> signUp(socialId, email, name, picture, socialType)); // 없으면 신규 회원가입

        // 4. 자체 JWT 토큰 생성 및 반환
        JwtTokenDTO jwtToken = jwtTokenProvider.generateToken(member);
        member.updateRefreshToken(jwtToken.getRefreshToken()); // 생성된 refreshToken DB 저장

        return LoginResponseDTO.builder()
                .role(member.getAuthorityKey())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }

    @Transactional
    public LoginResponseDTO loginWithKakao(String code){

        AccessTokenResponseDTO tokenDTO = kakaoOAuthService.getKakaoToken(code);
        KakaoInfoResponseDTO userInfo = kakaoOAuthService.getKakaoUserInfo(tokenDTO.getAccessToken());

        // 2. 사용자 정보 추출
        String socialId = userInfo.getId().toString();
        String name = userInfo.getKakaoAccount().getProfile().getName();
        String email = UUID.randomUUID() + "@socialUser.com";
        String picture = userInfo.getKakaoAccount().getProfile().getPicture();
        String socialType = "kakao";

        // 3. DB 처리 (회원가입 또는 로그인)
        Member member = memberRepository.findBySocialId(socialId)
                .map(entity -> entity.update(name, picture)) // 이미 있으면 정보 업데이트
                .orElseGet(() -> signUp(socialId, email, name, picture, socialType)); // 없으면 신규 회원가입

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
    private Member signUp(String socialId, String email, String name, String picture, String socialType) {

        // 랜덤 닉네임 생성
        String nickname = nicknameGenerator.generateUniqueNickname();

        Member newUser = Member.builder()
                .email(email)
                .name(name)
                .profileImage(picture)
                .nickname(nickname)
                .password("OAuth Password") // 임시 패스워드
                .socialId(socialId)
                .socialType(socialType)
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
                .readingTasteType(member.getReadingTasteType())
                .build();
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    @Transactional
    public JwtTokenDTO reissueToken(String refreshToken){

        // 1. Refresh Token 유효성 검증
        if(!jwtTokenProvider.validateToken(refreshToken)){
            throw new UnauthorizedException(ErrorStatus.TOKEN_UNAUTHORIZED.getMessage());
        }

        // 2. DB에서 해당 Refresh Token을 가진 회원 찾기
        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 3. 토큰 재발급 (Access/Refresh)
        JwtTokenDTO jwtToken = jwtTokenProvider.generateToken(member);

        // 4. DB에 새로 발급한 Refresh Token 저장
        member.updateRefreshToken(jwtToken.getRefreshToken());
        memberRepository.save(member);

        // 5. 재발급 토큰 반환
        return jwtToken;
    }

    // 닉네임 재생성
    @Transactional
    public NicknameResponseDTO regenerateNickname(String email) {
        Member member = getMemberByEmail(email);

        // 랜덤 닉네임 생성
        String newNickname = nicknameGenerator.generateUniqueNickname();

        // 닉네임 업데이트
        member.updateNickname(newNickname);

        return NicknameResponseDTO.builder()
                .nickname(newNickname)
                .build();
    }

    // 닉네임 직접 등록
    @Transactional
    public NicknameResponseDTO updateNickname(String email, NicknameRequestDTO nicknameRequestDTO) {
        Member member = getMemberByEmail(email);

        String nickname = nicknameRequestDTO.getNickname();

        // 닉네임 중복 체크
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new BadRequestException(ErrorStatus.NICKNAME_ALREADY_EXISTS_EXCEPTION.getMessage());
        }

        // 닉네임 업데이트
        member.updateNickname(nickname);

        return NicknameResponseDTO.builder()
                .nickname(nickname)
                .build();
    }

    // 닉네임 중복 체크
    @Transactional(readOnly = true)
    public NicknameCheckResponseDTO checkNicknameDuplicate(String nickname) {
        boolean isDuplicate = memberRepository.findByNickname(nickname).isPresent();

        return NicknameCheckResponseDTO.builder()
                .isDuplicate(isDuplicate)
                .build();
    }
}
