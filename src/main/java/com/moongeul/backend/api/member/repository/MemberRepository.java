package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialId(String socialId);

    Optional<Member> findByRefreshToken(String refreshToken);

    Optional<Member> findByNickname(String nickname);

    Page<Member> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);
}
