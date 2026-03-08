package com.sparta.omin.app.model.review.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ReviewCriteria {
    RATING_HIGH("rating", "평점 높은순", Sort.Direction.DESC),
    RATING_LOW("rating", "평점 낮은순", Sort.Direction.ASC),
    RECENT("createdAt", "최신순", Sort.Direction.DESC),
    OLDEST("createdAt", "오래된순", Sort.Direction.ASC),
    DEFAULT("createdAt", "기본pageable에 따름", Sort.Direction.DESC);
    private final String field;
    private final String description;
    private final Sort.Direction direction;

}
