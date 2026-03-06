package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 단건 조회 - 성공")
    void getOrderDetail_success() {
        // given
        UUID orderId = UUID.randomUUID();
        User user = mock(User.class);
        Store store = mock(Store.class);
        given(store.getId()).willReturn(UUID.randomUUID());
        given(store.getName()).willReturn("테스트 가게");

        // OrderItem Mock 추가
        OrderItem orderItem = mock(OrderItem.class);
        Product product = mock(Product.class);
        given(product.getName()).willReturn("김치찌개");
        given(orderItem.getProduct()).willReturn(product);
        given(orderItem.getQuantity()).willReturn(2);
        given(orderItem.getPrice()).willReturn(8000.0);
        given(orderItem.getTotalPrice()).willReturn(16000.0);

        Order order = Order.create(
                user,
                store,
                "문 앞에 놔주세요",
                "서울시 종로구 세종대로 172",
                "1층 로비"
        );

        order.getOrderItems().add(orderItem);

        given(orderRepository.findByIdAndIsDeletedFalse(orderId))
                .willReturn(Optional.of(order));

        // when
        OrderDetailResponse response = orderService.getOrderDetail(orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.address().shippingAddress()).isEqualTo("서울시 종로구 세종대로 172");
        assertThat(response.address().shippingDetailAddress()).isEqualTo("1층 로비");
        assertThat(response.store().storeName()).isEqualTo("테스트 가게");

        // 결과값 콘솔 출력
        System.out.println("=== 주문 조회 결과 ===");
        System.out.println("주문 상태: " + response.orderStatus());
        System.out.println("가게명: " + response.store().storeName());
        System.out.println("도로명 주소: " + response.address().shippingAddress());
        System.out.println("상세 주소: " + response.address().shippingDetailAddress());
        System.out.println("요청사항: " + response.userRequest());
        System.out.println("총 금액: " + response.totalPrice());

        System.out.println("=== 주문 아이템 ===");
        response.orderItems().forEach(item -> {
            System.out.println("상품명: " + item.productName());
            System.out.println("수량: " + item.quantity());
            System.out.println("단가: " + item.itemPrice());
            System.out.println("총액: " + item.totalPrice());
        });
    }
}