package com.moongeul.backend.api.story.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.entity.PostVisibility;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.story.dto.StoryAllResponseDTO;
import com.moongeul.backend.api.story.dto.StoryIdResponseDTO;
import com.moongeul.backend.api.story.dto.StoryDTO;
import com.moongeul.backend.api.story.entity.Story;
import com.moongeul.backend.api.story.repository.StoryRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import com.moongeul.backend.common.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoryService {

    private final MemberRepository memberRepository;
    private final StoryRepository storyRepository;
    private final PostRepository postRepository;
    private final FileUploadService fileUploadService;

    /* 스토리 제작 API */
    @Transactional
    public StoryIdResponseDTO createStory(String email, MultipartFile storyImage, Long postId) {
        validateStoryImage(storyImage);

        Member member = getMemberByEmail(email);
        Post post = getPost(postId);
        
        // 예외처리: 회원의 게시글이 아닌 경우
        if(post.getMember() != member){
            throw new UnauthorizedException(ErrorStatus.POST_UNAUTHORIZED.getMessage());
        }

        String uploadedStoryImageUrl = fileUploadService.uploadFile(storyImage, "profile/" + member.getId());

        Story newStory = Story.builder()
                .storyImage(uploadedStoryImageUrl)
                .member(member)
                .post(post)
                .build();

        Story savedStory = storyRepository.save(newStory);

        return StoryIdResponseDTO.builder()
                .storyId(savedStory.getId())
                .build();
    }

    private void validateStoryImage(MultipartFile storyImage) {
        if (storyImage == null || storyImage.isEmpty()) {
            throw new BadRequestException(ErrorStatus.PROFILE_IMAGE_EMPTY_EXCEPTION.getMessage());
        }

        String contentType = storyImage.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BadRequestException(ErrorStatus.PROFILE_IMAGE_INVALID_TYPE_EXCEPTION.getMessage());
        }
    }

    /* 스토리 전체 조회 API */
    @Transactional(readOnly = true)
    public StoryAllResponseDTO getALLStory(String email, PostVisibility postVisibility, Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Story> storyPage;

        // 현재 시간 기준 24시간 전 계산
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);

        boolean isAnonymous = (email == null || "anonymousUser".equals(email));

        if (isAnonymous) {
            // 비로그인: 전체 공개 스토리만 조회
            storyPage = storyRepository.findAllPublicStories(null, timeLimit, pageable);
        } else {
            // 로그인: 전체 공개 스토리 + 내 스토리
            if (PostVisibility.PUBLIC.equals(postVisibility)) {
                storyPage = storyRepository.findAllPublicStories(email, timeLimit, pageable);
            } else {
                // FOLLOWERS 공개 범위 선택 시: 팔로잉 스토리 + 내 스토리
                storyPage = storyRepository.findAllFollowerStories(email, timeLimit, pageable);
            }
        }

        List<StoryDTO> storyDTOList = storyPage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return StoryAllResponseDTO.builder()
                .total(storyPage.getTotalElements())
                .page(storyPage.getNumber() + 1)
                .size(storyPage.getSize())
                .totalPages(storyPage.getTotalPages())
                .isLast(storyPage.isLast())
                .data(storyDTOList)
                .build();
    }

    /* 스토리 상세 조회 API */
    @Transactional(readOnly = true)
    public StoryDTO getStory(Long storyId) {

        Story story = getStoryById(storyId);

        return convertToDTO(story);
    }

    // StoryDTO build 메서드
    private StoryDTO convertToDTO(Story story) {

        StoryDTO.MemberInfo memberInfo = StoryDTO.MemberInfo.builder()
                .memberId(story.getMember().getId())
                .nickname(story.getMember().getNickname())
                .profileImage(story.getMember().getProfileImage())
                .readingTasteType(story.getMember().getReadingTasteType())
                .build();

        StoryDTO.StoryInfo storyInfo = StoryDTO.StoryInfo.builder()
                .storyId(story.getId())
                .storyImage(story.getStoryImage())
                .created(story.getCreatedAt())
                .build();

        return StoryDTO.builder()
                .memberInfo(memberInfo)
                .storyInfo(storyInfo)
                .build();
    }

    /* 스토리 삭제 API */
    public void deleteStory(String email, Long storyId){

        Story story = getStoryById(storyId);

        if(!story.getMember().getEmail().equals(email)){
            throw new UnauthorizedException(ErrorStatus.STORY_UNAUTHORIZED.getMessage());
        }

        fileUploadService.deleteFileByUrl(story.getStoryImage());
        storyRepository.delete(story);
    }

    /*
    *
    * 코드 깔끔하게 하기용 메서드
    *
    * */

    // 회원 조회 메서드
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    // 스토리 조회 메서드
    private Story getStoryById(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.STORY_NOTFOUND_EXCEPTION.getMessage()));
    }

    // 기록(게시글) 조회 메서드
    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.POST_NOTFOUND_EXCEPTION.getMessage()));
    }
}
