package com.moongeul.backend.api.member.service;


import com.moongeul.backend.api.member.dto.AcceptFollowRequestDTO;
import com.moongeul.backend.api.member.dto.FollowResponseDTO;
import com.moongeul.backend.api.member.entity.Follow;
import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.member.repository.FollowRepository;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.notification.entity.NotificationType;
import com.moongeul.backend.api.notification.entity.Notifications;
import com.moongeul.backend.api.notification.repository.NotificationRepository;
import com.moongeul.backend.api.notification.service.NotificationTriggerService;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationTriggerService notificationTriggerService;

    /* 팔로우 API */
    @Transactional
    public void follow(Long following_id, String email){
        Member following = getMemberById(following_id); // 팔로우 대상
        Member follower = getMemberByEmail(email); // 나

        // 에러처리: 자기자신을 팔로우하는 경우 (불가)
        if(following.equals(follower)){
            throw new BadRequestException(ErrorStatus.SELF_FOLLOW_NOT_ALLOWED.getMessage());
        }

        // 에러처리: 이미 팔로우 중이거나 요청 대기 중인 경우
        followRepository.findByFollowingIdAndFollowerId(following.getId(), follower.getId())
                .ifPresent(follow -> {
                    if (follow.getFollowStatus() == FollowStatus.ACCEPTED) {
                        throw new BadRequestException(ErrorStatus.EXISTS_FOLLOW_ACCEPTED.getMessage());
                    } else if (follow.getFollowStatus() == FollowStatus.PENDING) {
                        throw new BadRequestException(ErrorStatus.EXISTS_FOLLOW_PENDING.getMessage());
                    }
                });

        // 상대방이 공개/일부공개/비공개 계정인지 확인하여 상태 결정
        FollowStatus status = following.getPrivacyLevel() == PrivacyLevel.PUBLIC ? FollowStatus.ACCEPTED : FollowStatus.PENDING;

        Follow newFollow = Follow.builder()
                .follower(follower)
                .following(following)
                .followStatus(status)
                .build();

        followRepository.save(newFollow);

        log.info("팔로우 완료 - 팔로우 대상 ID: {}, 작성자 ID: {}", following.getNickname(), follower.getNickname());

        notificationTriggerService.followNotification(following, follower); // 팔로우 알림 발생
    }

    /* 언팔로우 API - 이 경우, 승인 대기중 상태도 같이 삭제 */
    @Transactional
    public void unfollow(Long followingId, String email) {
        Member follower = getMemberByEmail(email); // 나

        // 예외처리: 팔로우 관계가 존재하지 않는 경우
        Follow follow = followRepository.findByFollowingIdAndFollowerId(followingId, follower.getId())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NO_FOLLOW_RELATIONSHIP.getMessage()));

        followRepository.delete(follow);

        log.info("언팔로우 완료 - 언팔로우 대상 ID: {}, 작성자 ID: {}", follow.getFollowing().getNickname(), follower.getNickname());
    }

    // 팔로잉 사용자 목록 조회
    @Transactional(readOnly = true) // 생성, 수정, 삭제가 없는 메서드
    public List<FollowResponseDTO> getFollowing(String email){
        Member me = getMemberByEmail(email);

        return followRepository.findByFollowings(me.getId())
                .stream()
                .map(follow -> {
                    Member following = follow.getFollowing();
                    return FollowResponseDTO.builder()
                            .id(following.getId())
                            .profileImage(following.getProfileImage())
                            .nickname(following.getNickname())
                            .readingTasteType(following.getReadingTasteType())
                            .myFollowStatus(FollowStatus.ACCEPTED)
                            .build();
                })
                .toList();
    }

    // 팔로워 사용자 목록 조회
    @Transactional(readOnly = true) // 생성, 수정, 삭제가 없는 메서드
    public List<FollowResponseDTO> getFollower(String email){
        Member me = getMemberByEmail(email);

        // 나를 팔로우하는 사람들(Follower) 목록 조회 (상태: ACCEPTED만)
        List<Follow> followers = followRepository.findByFollowers(me.getId());

        // 내가 누구를 팔로우하고 있는지(PENDING, ACCEPTED) 전체 목록을 Map으로 만듦
        // Map은 서비스 로직 안에서만 '검색용'으로 쓰임
        Map<Long, FollowStatus> myFollowStatusMap = followRepository.findAllByFollowerId(me.getId())
                .stream()
                .collect(Collectors.toMap(
                        follow -> follow.getFollowing().getId(), // Key: 상대방 ID
                        follow -> follow.getFollowStatus()       // Value: 나의 팔로우 상태
                ));

        return followers.stream()
                .map(follow -> {
                    Member target = follow.getFollower(); // 나를 팔로우한 그 사람

                    // Map에서 내가 이 사람을 팔로우 중인지 찾음, 없으면 NONE!
                    FollowStatus status = myFollowStatusMap.getOrDefault(target.getId(), FollowStatus.NONE);

                    return FollowResponseDTO.builder()
                            .id(target.getId())
                            .profileImage(target.getProfileImage())
                            .nickname(target.getNickname())
                            .readingTasteType(target.getReadingTasteType())
                            .myFollowStatus(status) // 결정된 상태값(NONE, PENDING, ACCEPTED) 주입
                            .build();
                })
                .toList();
    }

    /* 팔로우 승인 API */
    @Transactional
    public void acceptFollow(AcceptFollowRequestDTO acceptFollowRequestDTO, String email){

        Member member = getMemberByEmail(email);

        Follow follow = followRepository.findByFollowingIdAndFollowerId(member.getId(), acceptFollowRequestDTO.getFollowerId())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NO_FOLLOW_RELATIONSHIP.getMessage()));

        // 알림 상태를 바꿔주기 위해
        Notifications notifications = notificationRepository.findByReceiverIdAndActorIdAndType(member.getId(), acceptFollowRequestDTO.getFollowerId(), NotificationType.FOLLOW_PRIVATE)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOTIFICATION_NOTFOUND_EXCEPTION.getMessage()));

        if(acceptFollowRequestDTO.getStatus().equals("ACCEPT")){
            // '승인'한 경우 -> ACCEPTED 변경 + 알림 타입 변경
            follow.accept();
            notifications.switchNotificationType();
        } else if(acceptFollowRequestDTO.getStatus().equals("DELETE")){
            // '삭제'한 경우 -> 팔로우/알림 삭제
            followRepository.delete(follow);
            notificationRepository.delete(notifications);
        } else{
            throw new BadRequestException(ErrorStatus.BAD_FOLLOW_PROCESS_REQUEST.getMessage());
        }
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
