package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.bookshelf.repository.DoneReadBookshelfRepository;
import com.moongeul.backend.api.bookshelf.repository.WishReadBookshelfRepository;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.category.repository.CategoryRepository;
import com.moongeul.backend.api.member.dto.*;
import com.moongeul.backend.api.member.entity.*;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.repository.FollowRepository;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.member.repository.WithdrawalRepository;
import com.moongeul.backend.api.member.util.NicknameGenerator;
import com.moongeul.backend.api.notification.repository.DeviceTokenRepository;
import com.moongeul.backend.api.notification.repository.NotificationRepository;
import com.moongeul.backend.api.post.dto.CategoryPostListResponseDTO;
import com.moongeul.backend.api.post.dto.PostDTO;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.entity.Quote;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.post.repository.QuoteRepository;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.question.repository.AnswerRepository;
import com.moongeul.backend.api.question.repository.QuestionRepository;
import com.moongeul.backend.api.setting.repository.AgreeRepository;
import com.moongeul.backend.common.config.jwt.JwtTokenProvider;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.ForbiddenException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import com.moongeul.backend.common.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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
    private final WithdrawalRepository withdrawalRepository;
    private final NotificationRepository notificationRepository;
    private final AnswerRepository answerRepository;
    private final DoneReadBookshelfRepository doneReadBookshelfRepository;
    private final WishReadBookshelfRepository wishReadBookshelfRepository;
    private final QuestionRepository questionRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final AgreeRepository agreeRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final NicknameGenerator nicknameGenerator;
    private final FileUploadService fileUploadService;

    // 인가코드 받아 JWT로 교환 및 회원가입/로그인 처리
    @Transactional
    public LoginResponseDTO loginWithGoogle(String code, String type){

        // 1. 인가 코드로 Google Access Token 및 사용자 정보 획득
        AccessTokenResponseDTO tokenDTO = googleOAuthService.getGoogleToken(code, type);
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
                .memberId(member.getId())
                .role(member.getAuthorityKey())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .isReadingTaste(isReadingTaste)
                .build();
    }

    @Transactional
    public LoginResponseDTO loginWithKakao(String code, String type){

        AccessTokenResponseDTO tokenDTO = kakaoOAuthService.getKakaoToken(code, type);
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
                .memberId(member.getId())
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

        return memberRepository.save(newUser);
    }

    /* 사용자 정보 조회 */
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

        return UserInfoDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .nickname(member.getNickname())
                .readingTasteType(member.getReadingTasteType())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .myFollowStatus(myFollowStatus)
                .privacyLevel(member.getPrivacyLevel() == null ? PrivacyLevel.PUBLIC : member.getPrivacyLevel())
                .isPushEnabled(member.isPushEnabled())
                .build();
    }

    /* 로그아웃 API */
    @Transactional
    public void logout(String email, String deviceToken) {
        Member member = getMemberByEmail(email);

        // 리프레시 토큰 null 처리
        member.updateRefreshToken(null);

        // 현재 기기의 디바이스 토큰만 삭제 (알림 차단)
        if (deviceToken != null) {
            deviceTokenRepository.deleteByToken(deviceToken);
        }
    }

    /* 탈퇴하기 API - soft delete 처리 */
    @Transactional
    public void withdraw(String email, WithdrawalRequestDTO withdrawalRequestDTO) {

        Member member = getMemberByEmail(email);
        String profileImageUrl = member.getProfileImage();
        String targetDomain = "https://api-bucket.rhkr8521.com";

        /* 1. 벌크 삭제 쿼리 실행 (각 Repository에 작성된 @Modifying 쿼리 호출) - 호출 순서 중요! */
        // [팔로우] 내가 팔로우한 & 나를 팔로우한 사람들 삭제
        followRepository.deleteAllByFollowerId(member.getId());
        followRepository.deleteAllByFollowingId(member.getId());

        // [디바이스토큰]
        deviceTokenRepository.deleteAllByMemberId(member.getId());

        // [약관 동의]
        agreeRepository.deleteAllByMemberId(member.getId());

        // [알림] 내가 받은 알림만 삭제
        notificationRepository.deleteAllByReceiverId(member.getId());

        // [답변] 내가 쓴 답변 & 내 질문에 달린 답변 삭제
        answerRepository.deleteAllByMemberId(member.getId());
        answerRepository.deleteAllByQuestionMemberId(member.getId());

        // [질문]
        questionRepository.deleteAllByMemberId(member.getId());

        // [책장]
        doneReadBookshelfRepository.deleteAllByMemberId(member.getId());
        wishReadBookshelfRepository.deleteAllByMemberId(member.getId());

        // [게시글 & 인용구]
        quoteRepository.deleteAllByMemberId(member.getId()); // Post 이전에 삭제
        postRepository.deleteAllByMemberId(member.getId());

        // [카테고리] Post가 모두 삭제된 후 삭제 가능
        categoryRepository.deleteAllByMemberId(member.getId());

        // 2. 탈퇴 이력 저장 (나중에 통계 및 재가입 방지 체크에 사용)
        Withdrawal withdrawal = Withdrawal.builder()
                .email(email)
                .reason(withdrawalRequestDTO.getReason()) // 예: "기타"
                .detailReason(withdrawalRequestDTO.getDetailReason()) // 예: "이유작성완료"
                .withdrawalDate(LocalDateTime.now())
                .build();
        withdrawalRepository.save(withdrawal);

        // 버킷 내 이미지 삭제
        if (profileImageUrl != null && profileImageUrl.startsWith(targetDomain)) {
            log.info("탈퇴 회원 프로필 버킷 이미지 삭제: {}", profileImageUrl);
            fileUploadService.deleteFileByUrl(profileImageUrl);
        }
        // 공개 상태를 PRIVATE 으로 변경
        member.updatePrivacy(PrivacyLevel.PRIVATE);
        // 3. Member 정보 초기화
        member.withdrawMember();

        memberRepository.saveAndFlush(member);
    }

    /* 기록 통계 조회 (마이페이지 기록장) */
    @Transactional(readOnly = true)
    public PostStatsResponseDTO getPostStats(String email, Long userId) {

        Member currentMember = getMemberByEmail(email);
        Member targetMember = (userId == null)
                ? currentMember
                : memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        validatePrivacyAccess(currentMember, targetMember);

        Long memberId = targetMember.getId();

        // 전체 작성 갯수
        int totalPostCount = (int) postRepository.countByMemberId(memberId);

        // 해당 사용자가 만든 카테고리 목록 조회
        List<Category> categories = categoryRepository.findByMember(targetMember).orElse(new ArrayList<>());

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

    // 프로필 이미지 변경
    @Transactional
    public void updateProfileImage(String email, MultipartFile profileImage) {
        validateProfileImage(profileImage);

        Member member = getMemberByEmail(email);
        String previousProfileImage = member.getProfileImage();

        String uploadedProfileImageUrl = fileUploadService.uploadFile(profileImage, "profile/" + member.getId());
        member.updateProfileImage(uploadedProfileImageUrl);

        fileUploadService.deleteFileByUrl(previousProfileImage);
    }

    // 카테고리별 기록 리스트 조회
    @Transactional(readOnly = true)
    public CategoryPostListResponseDTO getCategoryPostList(String email, Long userId, Long categoryId, String sortBy, Integer page, Integer size) {

        Member currentMember = getMemberByEmail(email);
        Member targetMember = (userId == null)
                ? currentMember
                : memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        validatePrivacyAccess(currentMember, targetMember);

        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage()));

        // 카테고리 소유자 확인
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

        List<PostDTO> postList = postPage.getContent().stream()
                .map(this::convertToPostDTO)
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

    // 공감한 기록 리스트 조회
    @Transactional(readOnly = true)
    public CategoryPostListResponseDTO getLikedPostList(String email, Long userId, String sortBy, Integer page, Integer size) {

        Member currentMember = getMemberByEmail(email);
        Member targetMember = (userId == null)
                ? currentMember
                : memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        validatePrivacyAccess(currentMember, targetMember);

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Post> postPage = switch (sortBy.toUpperCase()) {
            case "LATEST" ->
                    postRepository.findLikedPostsByMemberIdOrderByCreatedAtDesc(targetMember.getId(), pageable);
            case "OLDEST" ->
                    postRepository.findLikedPostsByMemberIdOrderByCreatedAtAsc(targetMember.getId(), pageable);
            case "RATING_HIGH" ->
                    postRepository.findLikedPostsByMemberIdOrderByRatingDesc(targetMember.getId(), pageable);
            case "RATING_LOW" ->
                    postRepository.findLikedPostsByMemberIdOrderByRatingAsc(targetMember.getId(), pageable);
            default ->
                    postRepository.findLikedPostsByMemberIdOrderByCreatedAtDesc(targetMember.getId(), pageable);
        };

        List<PostDTO> postList = postPage.getContent().stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());

        log.info("공감한 기록 리스트 조회 완료 - 사용자 ID: {}, 정렬: {}, 페이지: {}, 결과 수: {}",
                targetMember.getId(), sortBy, page, postList.size());

        return CategoryPostListResponseDTO.builder()
                .total(postPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(postPage.getTotalPages())
                .isLast(postPage.isLast())
                .data(postList)
                .build();
    }

    // Post를 PostDTO로 변환
    private PostDTO convertToPostDTO(Post post) {

        Book book = post.getBook();

        PostDTO.MemberInfo memberInfo = PostDTO.MemberInfo.builder()
                .memberId(post.getMember().getId())
                .nickname(post.getMember().getNickname())
                .profileImage(post.getMember().getProfileImage())
                .readingTasteType(post.getMember().getReadingTasteType())
                .build();

        PostDTO.BookInfo bookInfo = PostDTO.BookInfo.builder()
                .isbn(book.getIsbn())
                .bookImage(book.getBookImage())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .pubdate(book.getPubdate())
                .ratingAverage(book.getRatingAverage())
                .build();

        List<Quote> quotes = quoteRepository.findByPostId(post.getId());
        List<PostDTO.QuoteDTO> quoteDTOList = quotes.stream()
                .map(quote -> PostDTO.QuoteDTO.builder()
                        .quoteContent(quote.getQuoteContent())
                        .pageNumber(quote.getPageNumber())
                        .build())
                .collect(Collectors.toList());

        PostDTO.LikesCnt likesCnt = PostDTO.LikesCnt.builder()
                .relatableCount(post.getRelatableCount())
                .sameTasteCount(post.getSameTasteCount())
                .impressiveExpressionCount(post.getImpressiveExpressionCount())
                .wantToReadCount(post.getWantToReadCount())
                .helpfulCount(post.getHelpfulCount())
                .build();

        return PostDTO.builder()
                .postId(post.getId())
                .memberInfo(memberInfo)
                .created(post.getCreatedAt())
                .bookInfo(bookInfo)
                .rating(post.getRating())
                .content(post.getContent())
                .readDate(post.getReadDate())
                .quotesCnt(quoteDTOList.size())
                .quotes(quoteDTOList)
                .likesCnt(likesCnt)
                .build();
    }

    private void validatePrivacyAccess(Member currentMember, Member targetMember) {
        if (currentMember.getId().equals(targetMember.getId())) {
            return;
        }

        PrivacyLevel privacyLevel = targetMember.getPrivacyLevel();

        if (privacyLevel == null || privacyLevel == PrivacyLevel.PUBLIC) {
            return;
        }

        if (privacyLevel == PrivacyLevel.PRIVATE) {
            throw new ForbiddenException(ErrorStatus.PRIVACY_FORBIDDEN_EXCEPTION.getMessage());
        }

        if (privacyLevel == PrivacyLevel.FOLLOWER_ONLY) {
            FollowStatus status = followRepository.findByFollowingIdAndFollowerId(targetMember.getId(), currentMember.getId())
                    .map(Follow::getFollowStatus)
                    .orElse(FollowStatus.NONE);

            if (status != FollowStatus.ACCEPTED) {
                throw new ForbiddenException(ErrorStatus.PRIVACY_FORBIDDEN_EXCEPTION.getMessage());
            }
        }
    }

    // 회원 조회 메서드
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private void validateProfileImage(MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new BadRequestException(ErrorStatus.PROFILE_IMAGE_EMPTY_EXCEPTION.getMessage());
        }

        String contentType = profileImage.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BadRequestException(ErrorStatus.PROFILE_IMAGE_INVALID_TYPE_EXCEPTION.getMessage());
        }
    }
}
