package com.moongeul.backend.api.setting.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.setting.dto.InfoOpenResponseDTO;
import com.moongeul.backend.api.setting.dto.InfoOpenUpdateRequestDTO;
import com.moongeul.backend.api.setting.entity.InfoOpen;
import com.moongeul.backend.api.setting.repository.InfoOpenRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InfoOpenService {

    private final InfoOpenRepository infoOpenRepository;
    private final MemberRepository memberRepository;

    // 계정 공개 여부 조회
    @Transactional
    public InfoOpenResponseDTO getInfoOpen(String email) {
        Member member = getMemberByEmail(email);
        InfoOpen infoOpen = infoOpenRepository.findByMemberId(member.getId())
                .orElseGet(() -> infoOpenRepository.save(InfoOpen.createDefault(member)));

        return toResponse(infoOpen);
    }

    // 계정 공개 여부 수정
    @Transactional
    public InfoOpenResponseDTO updateInfoOpen(String email, InfoOpenUpdateRequestDTO infoOpenUpdateRequestDTO) {
        validateSingleTrue(infoOpenUpdateRequestDTO);

        Member member = getMemberByEmail(email);
        InfoOpen infoOpen = infoOpenRepository.findByMemberId(member.getId())
                .orElseGet(() -> infoOpenRepository.save(InfoOpen.createDefault(member)));

        infoOpen.update(
                Boolean.TRUE.equals(infoOpenUpdateRequestDTO.getIsPublic()),
                Boolean.TRUE.equals(infoOpenUpdateRequestDTO.getIsFollowersOnly()),
                Boolean.TRUE.equals(infoOpenUpdateRequestDTO.getIsPrivate())
        );

        return toResponse(infoOpen);
    }

    // 헬퍼 메서드들
    private void validateSingleTrue(InfoOpenUpdateRequestDTO request) {
        int trueCount = 0;
        if (Boolean.TRUE.equals(request.getIsPublic())) trueCount++;
        if (Boolean.TRUE.equals(request.getIsFollowersOnly())) trueCount++;
        if (Boolean.TRUE.equals(request.getIsPrivate())) trueCount++;

        if (trueCount != 1) {
            throw new BadRequestException(ErrorStatus.INVALID_INFO_OPEN_EXCEPTION.getMessage());
        }
    }

    private InfoOpenResponseDTO toResponse(InfoOpen infoOpen) {
        return InfoOpenResponseDTO.builder()
                .isPublic(infoOpen.getIsPublic())
                .isFollowersOnly(infoOpen.getIsFollowersOnly())
                .isPrivate(infoOpen.getIsPrivate())
                .build();
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
