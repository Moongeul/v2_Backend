package com.moongeul.backend.api.story.controller;

import com.moongeul.backend.api.post.entity.PostVisibility;
import com.moongeul.backend.api.story.dto.StoryAllResponseDTO;
import com.moongeul.backend.api.story.dto.StoryIdResponseDTO;
import com.moongeul.backend.api.story.dto.StoryDTO;
import com.moongeul.backend.api.story.service.StoryService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Story", description = "Story(스토리) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/story")
public class StoryController {

    private final StoryService storyService;

    @Operation(
            summary = "스토리 제작 API",
            description = "스토리에서 제작한 '이미지' 파일을 저장합니다." +
                    "<br>- 해당 스토리와 연관된 게시글 ID(postId)값을 넘겨주세요."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스토리 제작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "업로드할 이미지가 없거나 이미지 형식이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "파일 업로드/삭제에 실패했습니다.")
    })
    @PostMapping(value = "/create/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StoryIdResponseDTO>> createStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("StoryImage") MultipartFile storyImage,
            @PathVariable Long postId) {

        StoryIdResponseDTO storyIdResponseDTO = storyService.createStory(userDetails.getUsername(), storyImage, postId);
        return ApiResponse.success(SuccessStatus.CREATE_STORY_SUCCESS, storyIdResponseDTO);
    }

    @Operation(
            summary = "스토리 전체 조회 API",
            description = "활성화된 스토리를 전체 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스토리 전체 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다."),
    })
    @GetMapping
    public ResponseEntity<ApiResponse<StoryAllResponseDTO>> getStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "PUBLIC") PostVisibility postVisibility,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.(1부터 시작)") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {

        // 비회원인 경우 "anonymousUser", 회원인 경우 email
        String username = (userDetails != null) ? userDetails.getUsername() : "anonymousUser";

        StoryAllResponseDTO storyAllResponseDTO = storyService.getALLStory(username, postVisibility, page, size);
        return ApiResponse.success(SuccessStatus.GET_ALL_STORY_SUCCESS, storyAllResponseDTO);
    }

    @Operation(
            summary = "스토리 상세 조회 API",
            description = "스토리를 상세 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스토리 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 스토리를 찾을 수 없습니다."),
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryDTO>> getStory(@PathVariable Long id) {

        StoryDTO storyDTO = storyService.getStory(id);
        return ApiResponse.success(SuccessStatus.GET_STORY_SUCCESS, storyDTO);
    }

    @Operation(
            summary = "스토리 삭제 API",
            description = "스토리를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스토리 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "회원의 스토리가 아닙니다."),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStory(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable Long id) {

        storyService.deleteStory(userDetails.getUsername(), id);
        return ApiResponse.success_only(SuccessStatus.DELETE_STORY_SUCCESS);
    }
}
