package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.category.repository.CategoryRepository;
import com.moongeul.backend.api.member.dto.*;
import com.moongeul.backend.api.member.entity.Follow;
import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.Follow;
import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.member.entity.Role;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.repository.FollowRepository;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.member.util.NicknameGenerator;
import com.moongeul.backend.api.post.dto.CategoryPostDetailDTO;
import com.moongeul.backend.api.post.dto.CategoryPostListResponseDTO;
import com.moongeul.backend.api.post.dto.LikeStatsDTO;
import com.moongeul.backend.api.post.dto.QuoteDTO;
import com.moongeul.backend.api.setting.dto.InfoOpenResponseDTO;
import com.moongeul.backend.api.setting.entity.InfoOpen;
import com.moongeul.backend.api.setting.repository.InfoOpenRepository;
import com.moongeul.backend.api.post.entity.Likes;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.entity.Quote;
import com.moongeul.backend.api.post.repository.LikeRepository;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.post.repository.QuoteRepository;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.common.config.jwt.JwtTokenProvider;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final QuoteRepository quoteRepository;
    private final LikeRepository likeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final NicknameGenerator nicknameGenerator;
    private final InfoOpenRepository infoOpenRepository;

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

        // 5. 취향테스트 수행 여부
        boolean isReadingTaste = (member.getReadingTasteType() != null);

        return LoginResponseDTO.builder()
                .role(member.getAuthorityKey())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .isReadingTaste(isReadingTaste)
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

        // 5. 취향테스트 수행 여부
        boolean isReadingTaste = (member.getReadingTasteType() != null);

        return LoginResponseDTO.builder()
                .role(member.getAuthorityKey())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .isReadingTaste(isReadingTaste)
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
                .privacyLevel(PrivacyLevel.PUBLIC) // 기본값: 전체공개
                .socialId(socialId)
                .socialType(socialType)
                .role(Role.GUEST) // 이후 필요 정보 모두 입력 시 USER 로 승격
                .build();
        Member savedMember = memberRepository.save(newUser);

        // 계정 공개 범위 기본값 생성 (전체 공개)
        infoOpenRepository.save(InfoOpen.createDefault(savedMember));

        return savedMember;
    }

    // 사용자 정보 조회
    @Transactional(readOnly = true)
    public UserInfoDTO getUserInfo(String email, Long userId){

        Member currentMember = getMemberByEmail(email);

        // userId가 null이면 본인 정보 조회, 있으면 타 사용자 조회
        Member member = (userId == null)
                ? currentMember
                : memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
      
        // 팔로워 수 계산 (나를 팔로우하는 사람들 중 승인된 경우)
        int followerCount = followRepository.findByFollowers(member.getId()).size();

        // 팔로잉 수 계산 (내가 팔로우한 사람들 중 승인된 경우)
        int followingCount = followRepository.findByFollowings(member.getId()).size();

        // 내가 해당 사용자를 팔로우했는지 여부
        FollowStatus myFollowStatus = FollowStatus.NONE;
        if (!currentMember.getId().equals(member.getId())) {
            myFollowStatus = followRepository.findByFollowingIdAndFollowerId(member.getId(), currentMember.getId())
                    .map(Follow::getFollowStatus)
                    .orElse(FollowStatus.NONE);
        }

        InfoOpen infoOpen = infoOpenRepository.findByMemberId(member.getId()).orElse(null);
        InfoOpenResponseDTO infoOpenResponse = (infoOpen == null)
                ? InfoOpenResponseDTO.builder()
                .isPublic(true)
                .isFollowersOnly(false)
                .isPrivate(false)
                .build()
                : InfoOpenResponseDTO.builder()
                .isPublic(infoOpen.getIsPublic())
                .isFollowersOnly(infoOpen.getIsFollowersOnly())
                .isPrivate(infoOpen.getIsPrivate())
                .build();

        return UserInfoDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .nickname(member.getNickname())
                .readingTasteType(member.getReadingTasteType())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .myFollowStatus(myFollowStatus)
                .infoOpen(infoOpenResponse)
                .build();
    }

    /* 기록 통계 조회 (마이페이지 기록장) */
    public PostStatsResponseDTO getPostStats(String email, Long userId) {

        // userId가 null이면 본인 정보 조회, 있으면 타 사용자 조회
        Member member;
        if (userId == null) {
            member = getMemberByEmail(email);
        } else {
            member = memberRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
        }

        Long memberId = member.getId();

        // 전체 작성 갯수
        int totalPostCount = (int) postRepository.countByMemberId(memberId);

        // 해당 사용자가 만든 카테고리 목록 조회
        List<Category> categories = categoryRepository.findByMember(member).orElse(new ArrayList<>());

        // 카테고리별 기록 갯수 계산
        List<CategoryPostCountDTO> categoryStats = categories.stream()
                .map(category -> {
                    int postCount = (int) postRepository.countByMemberIdAndCategoryId(memberId, category.getId());
                    return CategoryPostCountDTO.builder()
                            .categoryId(category.getId())
                            .categoryTitle(category.getTitle())
                            .postCount(postCount)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("기록 통계 조회 완료 - 사용자 ID: {}, 전체 기록 수: {}, 카테고리 수: {}",
                memberId, totalPostCount, categoryStats.size());

        return PostStatsResponseDTO.builder()
                .totalPostCount(totalPostCount)
                .data(categoryStats)
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

    // 카테고리별 기록 리스트 조회
    @Transactional(readOnly = true)
    public CategoryPostListResponseDTO getCategoryPostList(String email, Long userId, Long categoryId, String sortBy, Integer page, Integer size) {

        Member currentMember = getMemberByEmail(email);
        Member targetMember = (userId == null)
                ? currentMember
                : memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage()));

        // 카테고리 소유자 확인 (userId가 없으면 본인 기준)
        if (!category.getMember().getId().equals(targetMember.getId())) {
            throw new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage());
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        // 정렬 조건에 따라 조회
        Page<Post> postPage = switch (sortBy.toUpperCase()) {
            case "LATEST" ->  // 최신순
                    postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
            case "OLDEST" ->  // 오래된순
                    postRepository.findByCategoryIdOrderByCreatedAtAsc(categoryId, pageable);
            case "RATING_HIGH" ->  // 평점 높은순
                    postRepository.findByCategoryIdOrderByRatingDesc(categoryId, pageable);
            case "RATING_LOW" ->  // 평점 낮은순
                    postRepository.findByCategoryIdOrderByRatingAsc(categoryId, pageable);
            default ->  // 기본값: 최신순
                    postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        };

        List<CategoryPostDetailDTO> postList = postPage.getContent().stream()
                .map(this::convertToCategoryPostDetailDTO)
                .collect(Collectors.toList());

        log.info("카테고리별 기록 리스트 조회 완료 - 카테고리 ID: {}, 사용자 ID: {}, 정렬: {}, 페이지: {}, 결과 수: {}",
                categoryId, targetMember.getId(), sortBy, page, postList.size());

        return CategoryPostListResponseDTO.builder()
                .total(postPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(postPage.getTotalPages())
                .isLast(postPage.isLast())
                .data(postList)
                .build();
    }

    // Post를 CategoryPostDetailDTO로 변환
    private CategoryPostDetailDTO convertToCategoryPostDetailDTO(Post post) {

        Member member = post.getMember();
        Book book = post.getBook();

        // 인상깊은 구절 조회
        List<Quote> quotes = quoteRepository.findByPostId(post.getId());
        List<QuoteDTO> quoteDTOs = quotes.stream()
                .map(quote -> QuoteDTO.builder()
                        .quoteContent(quote.getQuoteContent())
                        .pageNumber(quote.getPageNumber())
                        .build())
                .collect(Collectors.toList());

        // 공감 통계 계산
        List<Likes> likes = likeRepository.findByPostId(post.getId());

        Map<String, Integer> likeTypeCount = new HashMap<>();
        likeTypeCount.put("RELATABLE", 0);
        likeTypeCount.put("SAME_TASTE", 0);
        likeTypeCount.put("IMPRESSIVE_EXPRESSION", 0);
        likeTypeCount.put("WANT_TO_READ", 0);
        likeTypeCount.put("HELPFUL", 0);

        for (Likes like : likes) {
            String likeTypeName = like.getLikeType().name();
            likeTypeCount.put(likeTypeName, likeTypeCount.get(likeTypeName) + 1);
        }

        LikeStatsDTO likeStats = LikeStatsDTO.builder()
                .likeTypeCount(likeTypeCount)
                .build();

        return CategoryPostDetailDTO.builder()
                .postId(post.getId())
                .profileImage(member.getProfileImage())
                .nickname(member.getNickname())
                .readingTasteType(member.getReadingTasteType())
                .createdAt(post.getCreatedAt())
                .content(post.getContent())
                .userRating(post.getRating())
                .readDate(post.getReadDate())
                .bookImage(book.getBookImage())
                .bookTitle(book.getTitle())
                .publisher(book.getPublisher())
                .bookRating(book.getRatingAverage())
                .quotes(quoteDTOs)
                .likeStats(likeStats)
                .build();
    }
}
