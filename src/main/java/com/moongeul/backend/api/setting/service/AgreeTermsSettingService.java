package com.moongeul.backend.api.setting.service;

import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.setting.repository.AgreeRepository;
import com.moongeul.backend.api.setting.repository.TermsRepository;
import com.moongeul.backend.api.setting.dto.AgreeTermsRequestDTO;
import com.moongeul.backend.api.setting.entity.Agree;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.setting.entity.Terms;
import com.moongeul.backend.api.setting.entity.TermsType;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgreeTermsSettingService {

    private final MemberRepository memberRepository;
    private final TermsRepository termsRepository;
    private final AgreeRepository agreeRepository;

    /* 약관동의 API */
    @Transactional
    public void agreeToTerms(String email, AgreeTermsRequestDTO agreeTermsRequestDTO){

        Member member = getMemberByEmail(email);

        // 예외처리: 필수 동의 여부 검증
        if (!agreeTermsRequestDTO.isServiceTermsAgree() || !agreeTermsRequestDTO.isPrivatePolicyAgree()) {
            throw new BadRequestException(ErrorStatus.DISAGREE_REQUIRED_TERM.getMessage());
        }

        // 1. 약관 타입과 DTO의 동의 여부 매핑
        Map<TermsType, Boolean> agreementData = Map.of(
                TermsType.SERVICE_TERMS_AGREE, agreeTermsRequestDTO.isServiceTermsAgree(),
                TermsType.PRIVACY_POLICY_AGREE, agreeTermsRequestDTO.isPrivatePolicyAgree(),
                TermsType.MARKETING_AGREE, agreeTermsRequestDTO.isMarketingAgree()
        );

        agreementData.forEach((type, isAgreed) -> {
            // 1. 해당 타입의 약관 마스터 정보 조회
            Terms terms = termsRepository.findByTermsType(type)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.TERMS_NOTFOUND_EXCEPTION.getMessage()));

            // 2. 기존 동의 내역이 있는지 조회
            agreeRepository.findByMemberAndTerms(member, terms)
                    .ifPresentOrElse(
                            // 이미 데이터가 있다면? -> 동의 여부 필드만 수정 (Dirty Checking 발생)
                            existingAgree -> existingAgree.updateAgreement(isAgreed),
                            // 데이터가 없다면? -> 새로 생성해서 저장
                            () -> {
                                Agree newAgree = Agree.builder()
                                        .member(member)
                                        .terms(terms)
                                        .isAgreed(isAgreed)
                                        .build();
                                agreeRepository.save(newAgree);
                            }
                    );
        });
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
