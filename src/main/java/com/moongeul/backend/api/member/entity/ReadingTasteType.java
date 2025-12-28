package com.moongeul.backend.api.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReadingTasteType {

    EMOTIONAL_REFLECTOR("감성 사색 정리러", "감성 새벽 독거노인"),
    CHATTY_READER("수다쟁이 책러", "문화센터 전도사"),
    TREND_HUNTER("신상 헌터", "신간과 트렌드만이 답이다"),
    SYSTEMATIC_READER("정리왕 서평러", "안 쓰면 안 읽은 거다"),
    IMMERSIVE_READER("넷플릭스급 몰입러", "드라마 오타쿠 몽상가"),
    SECRET_DIARIST("비밀 일기장 주인", "내 기록은 흑역사, 공개 불가"),
    GENRE_SPECIALIST("장르 고인물", "난 같은 장르만 조진다"),
    RANDOM_PICKER("랜덤 피커", "책 선택은 운명이다");


    private final String name;
    private final String intro;
}
