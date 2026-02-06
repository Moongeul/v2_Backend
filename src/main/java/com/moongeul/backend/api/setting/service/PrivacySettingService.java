package com.moongeul.backend.api.setting.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.setting.dto.PrivacyLevelResponseDTO;
import com.moongeul.backend.api.setting.dto.PrivacyLevelUpdateRequestDTO;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrivacySettingService {

    private final MemberRepository memberRepository;

    // 계정 공개 범위 조회
    @Transactional(readOnly = true)
    public PrivacyLevelResponseDTO getPrivacyLevel(String email) {
        Member member = getMemberByEmail(email);
        return PrivacyLevelResponseDTO.builder()
                .privacyLevel(member.getPrivacyLevel())
                .build();
    }

    // 계정 공개 범위 수정
    @Transactional
    public PrivacyLevelResponseDTO updatePrivacyLevel(String email, PrivacyLevelUpdateRequestDTO request) {
        Member member = getMemberByEmail(email);
        PrivacyLevel privacyLevel = request.getPrivacyLevel();
        member.updatePrivacy(privacyLevel);

        return PrivacyLevelResponseDTO.builder()
                .privacyLevel(member.getPrivacyLevel())
                .build();
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
