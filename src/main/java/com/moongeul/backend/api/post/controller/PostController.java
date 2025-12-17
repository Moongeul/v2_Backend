package com.moongeul.backend.api.post.controller;

import com.moongeul.backend.api.post.dto.PostCreateRequestDTO;
import com.moongeul.backend.api.post.dto.PostCreateResponseDTO;
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
                    "<br>필수: isbn, readDate / 선택: rating(default = 5.0), page(default = 300), content, quotes" +
                    "<br>선택 요소의 경우 입력되지 않았을 때 'null'로 전달 바랍니다." +
                    "<br><br>[enum] postVisibility -> 전체 공개 : PUBLIC, 팔로워 공개 : FOLLOWERS, 나만보기 : PRIVATE"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "글쓰기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ISBN은 필수입니다. (isbn)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PostCreateResponseDTO>> createPost(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @Valid @RequestBody PostCreateRequestDTO postCreateRequestDTO) {

        PostCreateResponseDTO response = postService.createPost(postCreateRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CREATE_POST_SUCCESS, response);
    }
}
