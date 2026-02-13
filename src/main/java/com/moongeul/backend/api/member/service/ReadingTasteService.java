package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.TestRequestDTO;
import com.moongeul.backend.api.member.dto.TestResponseDTO;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.ReadingTasteType;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingTasteService {

    private final MemberRepository memberRepository;

    private static final String ANSWER_A = "A";
    private static final String ANSWER_B = "B";

    @Transactional
    public TestResponseDTO calculateReadingTasteType(TestRequestDTO testRequestDTO, String email){

        // 테스트 결과 계산 (가장 높은 유형 반환)
        ReadingTasteType type = findTopType(calculateScore(testRequestDTO.getAnswers()));

        // 회원이라면 취향테스트 결과 DB 저장
        if(!email.equals("anonymousUser")){
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

            // member 필드 readingTasteType 저장
            member.updateReadingTasteType(type);
            memberRepository.save(member);
        }

        return TestResponseDTO.builder()
                .readingTasteType(type.getName())
                .intro(type.getIntro())
                .build();
    }

    // 점수표 - 입력
    private Map<ReadingTasteType, Double> calculateScore(Map<Integer, String> answers){

        // 테스트 요청 데이터 검증(에러처리)
        validateAnswers(answers);

        Map<ReadingTasteType, Double> typeScores = new HashMap<>();

        for(Map.Entry<Integer, String> entry : answers.entrySet()){
            int questionNo = entry.getKey();
            String answer = entry.getValue();

            switch (questionNo) {
                case 1:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.EMOTIONAL_REFLECTOR, 6.01);
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 3.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.IMMERSIVE_READER, 6.50);
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 2.50);
                    }
                    break;
                case 2:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 6.01);
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 4.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 6.01);
                        addScore(typeScores, ReadingTasteType.CHATTY_READER, 3.99);
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 2.99);
                    }
                    break;
                case 3:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 6.01);
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 2.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.SYSTEMATIC_READER, 6.01);
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 3.99);
                    }
                    break;
                case 4:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 6.01);
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 3.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 6.01);
                        addScore(typeScores, ReadingTasteType.EMOTIONAL_REFLECTOR, 4.49);
                    }
                    break;
                case 5:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.EMOTIONAL_REFLECTOR, 6.51);
                        addScore(typeScores, ReadingTasteType.IMMERSIVE_READER, 4.49);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.CHATTY_READER, 6.51);
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 3.49);
                    }
                    break;
                case 6:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.SYSTEMATIC_READER, 5.51);
                        addScore(typeScores, ReadingTasteType.EMOTIONAL_REFLECTOR, 4.49);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 5.51);
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 4.49);
                    }
                    break;
                case 7:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.SYSTEMATIC_READER, 7.01);
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 3.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.CHATTY_READER, 6.01);
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 2.50);
                    }
                    break;
                case 8:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 7.50);
                        addScore(typeScores, ReadingTasteType.SYSTEMATIC_READER, 3.50);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.CHATTY_READER, 7.50);
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 2.50);
                    }
                    break;
                case 9:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.CHATTY_READER, 6.01);
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 4.01);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.IMMERSIVE_READER, 6.01);
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 4.01);
                    }
                    break;
                case 10:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 5.51);
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 3.49);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.SYSTEMATIC_READER, 7.51);
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 3.49);
                    }
                    break;
                case 11:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.IMMERSIVE_READER, 6.01);
                        addScore(typeScores, ReadingTasteType.GENRE_SPECIALIST, 3.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.EMOTIONAL_REFLECTOR, 6.00);
                        addScore(typeScores, ReadingTasteType.SECRET_DIARIST, 4.00);
                    }
                    break;
                case 12:
                    if(answer.equals(ANSWER_A)){
                        addScore(typeScores, ReadingTasteType.TREND_HUNTER, 6.01);
                        addScore(typeScores, ReadingTasteType.RANDOM_PICKER, 4.99);
                    } else if(answer.equals(ANSWER_B)){
                        addScore(typeScores, ReadingTasteType.IMMERSIVE_READER, 6.00);
                        addScore(typeScores, ReadingTasteType.EMOTIONAL_REFLECTOR, 4.00);
                    }
                    break;
                default:
                    log.error("잘못된 questionNo(1-12) 입력: {}번", questionNo);
                    throw new BadRequestException(
                            String.format(ErrorStatus.INVALID_QUESTION_NUMBER.getMessage(), questionNo)
                    );
            }
        }

        return typeScores;
    }

    // 검증 로직(에러처리) 분리
    private void validateAnswers(Map<Integer, String> answers){
        // 비어 있는 답변(null 또는 empty 체크)
        if(answers == null || answers.isEmpty()){
            log.error("테스트 요청 데이터가 비어있음.");
            throw new BadRequestException(ErrorStatus.EMPTY_TEST_ANSWERS.getMessage());
        }

        // 답변 개수 체크
        if(answers.size() != 12) {
            log.error("테스트 답변 개수(총 12개) 부족 (현재 답변 개수: {}개)", answers.size());
            throw new BadRequestException(
                    String.format(ErrorStatus.INCOMPLETE_TEST_ANSWERS.getMessage(), answers.size())
            );
        }

        // A/B 체크
        for(Map.Entry<Integer, String> entry : answers.entrySet()){
            int questionNo = entry.getKey();
            String answer = entry.getValue();

            if(!answer.equals(ANSWER_A) && !answer.equals(ANSWER_B)){
                log.error("테스트 답변은 A 또는 B여야 합니다.(현재 질문번호: {}, 답변: {})", questionNo, answer);
                throw new BadRequestException(
                        String.format(ErrorStatus.INVALID_ANSWER_VALUE.getMessage(), questionNo, answer)
                );
            }
        }
    }

    // 점수 계산
    private void addScore(Map<ReadingTasteType, Double> typeScores,
                          ReadingTasteType type, double score){
        typeScores.put(type, typeScores.getOrDefault(type, 0.0) + score);
    }

    // 가장 점수가 높은 type 찾기
    private ReadingTasteType findTopType(Map<ReadingTasteType, Double> typeScores){
        ReadingTasteType topType = null;
        double maxScore = Double.MIN_VALUE;  // 가장 작은 값으로 시작

        for (Map.Entry<ReadingTasteType, Double> entry : typeScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                topType = entry.getKey();
            }
        }

        if (topType == null) {
            log.info("점수 계산 결과가 없음, topType = {}", topType);
            throw new BadRequestException("점수 계산 결과가 없습니다.");
        }

        return topType;
    }
}
