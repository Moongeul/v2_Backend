package com.moongeul.backend.api.member.service;


import com.moongeul.backend.api.member.dto.UserInfoDTO;
import com.moongeul.backend.api.member.entity.Follow;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.FollowRepository;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 팔로우/언팔로우
    @Transactional
    public void follow(Long following_id, String email){
        Member following_member = getById(following_id);
        Member follower_member = getMemberByEmail(email);

        // 에러처리: 자기자신을 팔로우하는 경우 (불가)
        if(following_member.equals(follower_member)){
            throw new BadRequestException(ErrorStatus.SELF_FOLLOW_NOT_ALLOWED.getMessage());
        }

        Optional<Follow> existingFollow = followRepository.findByFollowingIdAndFollowerId(following_member.getId(), follower_member.getId());

        // 이미 팔로우하고 있는 사용자라면 -> 팔로우 취소
        if(existingFollow.isPresent()){
            Follow currentFollow = existingFollow.get();
            followRepository.delete(currentFollow);
        } else{ // 처음 팔로우 -> 팔로우 성공
            Follow new_follow = Follow.builder()
                    .following(following_member)
                    .follower(follower_member)
                    .build();

            followRepository.save(new_follow);
        }
    }

    // 팔로잉 사용자 목록 조회 // 생성, 수정, 삭제가 없는 메서드
    @Transactional(readOnly = true)
    public List<UserInfoDTO> getFollowing(String email){
        Member follower_member = getMemberByEmail(email);

        return followRepository.findByFollowings(follower_member.getId())
                .stream()
                .map(follow -> {
                    Member following = follow.getFollowing();
                    return UserInfoDTO.builder()
                            .id(following.getId())
                            .name(following.getName())
                            .profileImage(following.getProfileImage())
                            .nickname(following.getNickname())
                            .readingTasteType(following.getReadingTasteType())
                            .build();
                })
                .toList();
    }

    // 팔로워 사용자 목록 조회
    @Transactional(readOnly = true) // 생성, 수정, 삭제가 없는 메서드
    public List<UserInfoDTO> getFollower(String email){
        Member following_member = getMemberByEmail(email);

        return followRepository.findByFollowers(following_member.getId())
                .stream()
                .map(follow -> {
                    Member follower = follow.getFollower();
                    return UserInfoDTO.builder()
                            .id(follower.getId())
                            .name(follower.getName())
                            .profileImage(follower.getProfileImage())
                            .nickname(follower.getNickname())
                            .readingTasteType(follower.getReadingTasteType())
                            .build();
                })
                .toList();
    }

    private Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
