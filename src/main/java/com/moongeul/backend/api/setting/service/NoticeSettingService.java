package com.moongeul.backend.api.setting.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.Role;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.setting.dto.NoticeAllRequestDTO;
import com.moongeul.backend.api.setting.dto.NoticeAllResponseDTO;
import com.moongeul.backend.api.setting.dto.NoticeDTO;
import com.moongeul.backend.api.setting.dto.NoticeRequestDTO;
import com.moongeul.backend.api.setting.entity.Notice;
import com.moongeul.backend.api.setting.repository.NoticeRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeSettingService {

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;

    /* 공지사항 전체 조회 API */
    @Transactional(readOnly = true)
    public NoticeAllResponseDTO getAllNotice(Integer page, Integer size){

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notice> noticePage = noticeRepository.findAll(pageable); // Repository에서 페이징된 엔티티 결과 가져옴

        List<NoticeDTO> noticeDTOList = noticePage.getContent().stream()
                .map(notice -> NoticeDTO.builder()
                        .noticeId(notice.getId())
                        .title(notice.getTitle())
                        .content(notice.getContent())
                        .uploadDate(notice.getCreatedAt().toLocalDate()) // LocalDateTime -> LocalDate 변환
                        .build())
                .toList();

        return NoticeAllResponseDTO.builder()
                .total(noticePage.getTotalElements())   // 전체 게시글 수
                .page(noticePage.getNumber() + 1)       // 현재 페이지 번호(페이지 1부터 시작(임의 지정))
                .size(noticePage.getSize())             // 페이지당 데이터 개수
                .totalPages(noticePage.getTotalPages()) // 전체 페이지 수
                .isLast(noticePage.isLast())            // 마지막 페이지 여부
                .data(noticeDTOList)                   // 변환된 DTO 리스트
                .build();
    }

    /* 공지사항 상세 조회 API */
    @Transactional(readOnly = true)
    public NoticeDTO getNotice(Long id){
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOTICE_NOTFOUND_EXCEPTION.getMessage()));

        return NoticeDTO.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .uploadDate(LocalDate.from(notice.getCreatedAt()))
                .build();
    }

    /* 공지사항 작성 API */
    @Transactional
    public NoticeDTO createNotice(String email, NoticeRequestDTO noticeRequestDTO){
        Member member = getMemberByEmail(email);

        // 예외처리: 공지사항 작성자가 ADMIN이 아닌 경우
        if(member.getRole() != Role.ADMIN){
            throw new UnauthorizedException(ErrorStatus.CREATE_NOTICE_UNAUTHORIZED.getMessage());
        }

        Notice newNotice = noticeRequestDTO.toEntity();
        Notice savedNotice = noticeRepository.save(newNotice);

        return NoticeDTO.builder()
                .noticeId(savedNotice.getId())
                .title(savedNotice.getTitle())
                .content(savedNotice.getContent())
                .uploadDate(LocalDate.from(savedNotice.getCreatedAt()))
                .build();
    }

    /* 공지사항 삭제 API */
    @Transactional
    public void deleteNotice(String email, Long id){
        Member member = getMemberByEmail(email);

        // 예외처리: 공지사항 작성자가 ADMIN이 아닌 경우
        if(member.getRole() != Role.ADMIN){
            throw new UnauthorizedException(ErrorStatus.DELETE_NOTICE_UNAUTHORIZED.getMessage());
        }

        noticeRepository.deleteById(id);
    }

    /*
     * 단순 데이터 불러오기용 코드 메서드 - 코드 깔끔하게 하기용
     */

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
