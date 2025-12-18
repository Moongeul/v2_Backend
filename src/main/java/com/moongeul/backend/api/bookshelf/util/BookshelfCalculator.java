package com.moongeul.backend.api.bookshelf.util;

import org.springframework.stereotype.Component;

@Component
public class BookshelfCalculator {

    // 페이지 수에 따른 weight(두께) 계산
    public Float calculateWeight(Integer page) {
        if (page == null || page <= 0) {
            return 24.0f; // 기본값
        }

        if (page <= 100) {
            return 24.0f;
        } else if (page <= 150) {
            return 28.0f;
        } else if (page <= 200) {
            return 32.0f;
        } else if (page <= 250) {
            return 36.0f;
        } else if (page <= 300) {
            return 40.0f;
        } else if (page <= 350) {
            return 44.0f;
        } else if (page <= 400) {
            return 48.0f;
        } else if (page <= 450) {
            return 52.0f;
        } else if (page <= 500) {
            return 56.0f;
        } else if (page <= 600) {
            return 60.0f;
        } else if (page <= 800) {
            return 64.0f;
        } else if (page <= 1000) {
            return 68.0f;
        } else {
            return 72.0f; // 1001쪽 이상 (최대 고정)
        }
    }

    // 별점에 따른 height(높이) 계산
    public Float calculateHeight(Double rating) {
        if (rating == null || rating < 0.0) {
            return 88.0f; // 기본값 (0.0)
        }

        // 0.5 단위로 반올림
        double roundedRating = Math.round(rating * 2) / 2.0;
        
        // 5.0을 초과하면 5.0으로 제한
        if (roundedRating > 5.0) {
            roundedRating = 5.0;
        }

        // 별점에 따른 높이 계산 (0.5점당 4px 증가)
        return 88.0f + (float)(roundedRating * 8);
    }
}

