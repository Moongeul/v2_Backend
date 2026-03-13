package com.moongeul.backend.api.category.service;

import com.moongeul.backend.api.category.dto.CategoryCreateRequestDTO;
import com.moongeul.backend.api.category.dto.CategoryListResponseDTO;
import com.moongeul.backend.api.category.dto.CategoryResponseDTO;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.category.repository.CategoryRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    /* 카테고리 생성 */
    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO categoryCreateRequestDTO, String email){

        Member member = getMemberByEmail(email);

        Category newCategory = categoryCreateRequestDTO.toEntity(member);
        Category savedCategory = categoryRepository.save(newCategory);

        return CategoryResponseDTO.builder()
                .categoryId(savedCategory.getId())
                .title(savedCategory.getTitle())
                .build();
    }

    /* 내 카테고리 전체 조회 */
    @Transactional
    public CategoryListResponseDTO getCategory(String email){

        Member member = getMemberByEmail(email);

        List<Category> categoryList = categoryRepository.findByMember(member)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage()));

        List<CategoryResponseDTO> categoryResponseDTOList = categoryList.stream()
                .map(category -> CategoryResponseDTO.builder()
                        .categoryId(category.getId())
                        .title(category.getTitle())
                        .build())
                .toList();

        return CategoryListResponseDTO.builder()
                .categoryList(categoryResponseDTOList)
                .build();
    }

    /* 카테고리명 조회 */
    @Transactional
    public CategoryResponseDTO getCategoryTitle(Long id){

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage()));

        return CategoryResponseDTO.builder()
                .categoryId(category.getId())
                .title(category.getTitle())
                .build();
    }

    // 사용자 정보 가져오기 메서드
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
