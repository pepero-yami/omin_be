package com.sparta.omin.app.model.order.entity.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기중"),
    ACCEPTED("수락"),
    CANCELLED("취소"),
    COOKING("조리중"),
    COMPLETED("완료");

    private final String description;
}
