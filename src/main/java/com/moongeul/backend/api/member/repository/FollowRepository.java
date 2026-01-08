package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowingIdAndFollowerId(Long followingId, Long followerId);

    @Query("select f from Follow f join fetch f.following where f.follower.id = :followerId")
    List<Follow> findByFollowings(@Param("followerId") Long followerId);

    @Query("select f from Follow f join fetch f.follower where f.following.id = :followingId")
    List<Follow> findByFollowers(@Param("followingId") Long followingId);
}
