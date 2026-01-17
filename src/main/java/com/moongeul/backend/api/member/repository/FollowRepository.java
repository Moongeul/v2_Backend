package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 1. 특정 관계 조회
    Optional<Follow> findByFollowingIdAndFollowerId(Long followingId, Long followerId);

    // 2. 팔로잉 목록: 내가 팔로우한 사람들 중 '승인 완료(ACCEPTED)'인 경우만 조회
    @Query("select f from Follow f join fetch f.following " +
            "where f.follower.id = :followerId and f.followStatus = 'ACCEPTED'")
    List<Follow> findByFollowings(@Param("followerId") Long followerId);

    // 3. 팔로워 목록: 나를 팔로우하는 사람들 중 '승인 완료(ACCEPTED)'인 경우만 조회
    @Query("select f from Follow f join fetch f.follower " +
            "where f.following.id = :followingId and f.followStatus = 'ACCEPTED'")
    List<Follow> findByFollowers(@Param("followingId") Long followingId);

    // 4. 내가 팔로우를 건 모든 기록 (상태 상관 x)
    // 팔로워 목록에서 '내가 그들에게 보낸 상태'를 확인하기 위해 필요
    List<Follow> findAllByFollowerId(Long followerId);
}
