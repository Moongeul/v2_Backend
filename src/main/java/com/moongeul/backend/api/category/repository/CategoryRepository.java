package com.moongeul.backend.api.category.repository;


import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>  {

    Optional<List<Category>> findByMember(Member member);

    // 회원 탈퇴 시 해당 회원이 생성한 모든 카테고리 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Category c WHERE c.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
