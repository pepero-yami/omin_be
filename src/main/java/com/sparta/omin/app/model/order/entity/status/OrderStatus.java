package com.sparta.omin.app.model.order.entity.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기중", "주문이 접수되었습니다. 사장님의 확인을 기다리고 있습니다."),
    ACCEPTED("수락", "주문이 수락되었습니다. 곧 조리가 시작됩니다."),
    COOKING("조리중", "현재 조리가 진행중입니다. 조금만 기다려주세요!"),
    COMPLETED("완료", "주문이 완료되었습니다. 이용해주셔서 감사합니다."),
    CANCELLED("취소", "고객님의 요청으로 주문이 취소되었습니다. 다음에 또 이용해주세요."),
    REJECT("거절", "죄송합니다. 사정에 의해 주문이 거절되었습니다. 불편을 드려 죄송합니다.");

    private final String description;
    private final String mailMessage;
}
