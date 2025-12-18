package com.moongeul.backend.api.category.repository;


import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>  {

    Optional<List<Category>> findByMember(Member member);
}
