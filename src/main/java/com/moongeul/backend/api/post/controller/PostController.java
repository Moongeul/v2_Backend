package com.moongeul.backend.api.post.controller;

import com.moongeul.backend.api.post.dto.PostRequestDTO;
import com.moongeul.backend.api.post.dto.PostIdResponseDTO;
import com.moongeul.backend.api.post.dto.PostResponseDTO;
import com.moongeul.backend.api.post.service.PostService;
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

@Tag(name = "Post", description = "Post(게시글) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/post")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "글쓰기 API",
            description = "기록(게시글)을 작성하는 글쓰기 API 입니다." +
                    "<br><br>드롭박스 - 선택: postVisibility(default = PUBLIC), categoryId(default = 0(전체보기)) -> 입력되지 않았을 경우, 각각의 기본값(postVisibility: PUBLIC, categoryId: 0)으로 전달 바랍니다." +
                    "<br>필수: isbn, readDate / 선택: rating(default = 5.0), page(default = 300), content, quotes -> 입력되지 않았을 경우, 'null'로 전달 바랍니다." +
                    "<br><br>[enum] postVisibility -> 전체 공개 : PUBLIC, 팔로워 공개 : FOLLOWERS, 나만보기 : PRIVATE"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "글쓰기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ISBN은 필수입니다. (isbn)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PostIdResponseDTO>> createPost(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @Valid @RequestBody PostRequestDTO postRequestDTO) {

        PostIdResponseDTO response = postService.createPost(postRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CREATE_POST_SUCCESS, response);
    }

    @Operation(
            summary = "기록(게시글) 상세 조회 API",
            description = "기록(게시글)의 상세 조회 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록(게시글) 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 기록(게시글)을 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPost(@PathVariable Long id) {

        PostResponseDTO response = postService.getPostDetail(id);
        return ApiResponse.success(SuccessStatus.GET_POST_SUCCESS, response);
    }

    @Operation(
            summary = "기록(게시글) 수정 API",
            description = "기록(게시글)을 수정하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록(게시글) 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "수정하려는 회원의 게시글이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 기록(게시글)을 찾을 수 없습니다.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostIdResponseDTO>> updatePost(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @PathVariable Long id,
                                                                   @Valid @RequestBody PostRequestDTO postRequestDTO) {

        PostIdResponseDTO response = postService.updatePost(id, userDetails.getUsername(), postRequestDTO);
        return ApiResponse.success(SuccessStatus.UPDATE_POST_SUCCESS, response);
    }

    @Operation(
            summary = "기록(게시글) 삭제 API",
            description = "기록(게시글)을 삭제하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록(게시글) 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 기록(게시글)을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @PathVariable Long id) {

        postService.deletePost(id, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.DELETE_POST_SUCCESS);
    }
}
