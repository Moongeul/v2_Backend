package com.moongeul.backend.api.setting.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.setting.dto.PushStatusResponseDTO;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushSettingService {

    private final MemberRepository memberRepository;

    /* 푸시 알림 허용 on/off */
    @Transactional
    public void updatePushSetting(String email, boolean isPushEnabled) {
        Member member = getMemberByEmail(email);
        member.updatePushEnabled(isPushEnabled);
        log.info("사용자 id: {}, Nickname: {}의 푸시 설정이 {}로 변경되었습니다.", member.getId(), member.getNickname(), isPushEnabled);
    }

    /* 푸시 알림 동의 여부 조회 */
    @Transactional
    public PushStatusResponseDTO getPushStatus(String email){
        Member member = getMemberByEmail(email);
        return PushStatusResponseDTO.builder()
                .pushEnabled(member.isPushEnabled())
                .build();
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
