package com.sparta.omin.app.model.review.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ReviewCriteria {
    RATING_HIGH("평점 높은순", "rating", Sort.Direction.DESC),
    RATING_LOW("평점 낮은순", "rating", Sort.Direction.ASC),
    RECENT("최신순", "createdAt", Sort.Direction.DESC),
    OLDEST("오래된순", "createdAt", Sort.Direction.DESC);

    private final String description;
    private final String field;
    private final Sort.Direction direction;

}
