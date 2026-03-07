package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.dto.OrderCreateRequest;
import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.dto.OrderInternalDto;
import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
//    private final StoreReadService storeReadService;
    public OrderResponse createOrder(UUID userId, OrderCreateRequest request) {
        return null;
    }

    public OrderDetailResponse getOrderDetail(UUID orderId) {
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
        return OrderDetailResponse.from(order);
    }

    public Slice<OrderResponse> getOrdersHistory(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdAndIsDeletedFalse(userId, pageable)
                .map(OrderResponse::from);
    }

    public Slice<OrderResponse> getOrdersByOwner(UUID storeId, UUID userId, String email, Pageable pageable) {
        /**
         * TODO 검증 pull받으면 활성화 할 것!
         *
         *        if(!storeReadService.isOwnedStore(storeId(), email)) {
         *             throw new CommonException(ErrorCode.STORE_ACCESS_DENIED);
         *         }
         */

        // 가게에 들어온 주문 요청목록
        return orderRepository.findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(storeId, pageable)
                .map(OrderResponse::from);
    }

    // payment 서비스에서 주문 존재 여부와 데이터를 확인하기 위한 메서드
    public Order getOrderEntity(UUID orderId) {
        return orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public OrderInternalDto getOrderForPayment(UUID orderId) {
        Order order = getOrderEntity(orderId); // 기존에 만든 엔티티 조회 메서드 활용
        return new OrderInternalDto(
                order.getId(),
                order.getUser().getId(),
                order.getTotalPrice()
        );
    }

    //FIXME 리스너 사용 확정이면 이 부분 삭제!
//    // 테스트 API 및 리스너에서 상태 변경을 위해 호출
//    @Transactional
//    public void updateOrderStatus(UUID orderId, OrderStatus status) {
//        Order order = getOrderEntity(orderId);
//        order.updateStatus(status);
//    }
}
