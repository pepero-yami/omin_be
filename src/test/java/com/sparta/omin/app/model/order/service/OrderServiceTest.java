package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.dto.OrderResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 이력 조회 - 성공")
    void getOrderHistory_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Store store = mock(Store.class);

        Order order1 = Order.create(user, store, "문 앞에 놔주세요", "서울시 종로구 세종대로 172", "정부서울청사 1층");
        Order order2 = Order.create(user, store, "벨 눌러주세요", "서울시 종로구 사직로 161", "경복궁 옆 빌딩 2층");

        Pageable pageable = PageRequest.of(0, 10);
        SliceImpl<Order> slice = new SliceImpl<>(
                List.of(order1, order2),
                pageable,
                false
        );

        given(orderRepository.findByUserIdAndIsDeletedFalse(userId, pageable))
                .willReturn(slice);

        // when
        Slice<OrderResponse> response = orderService.getOrdersHistory(userId, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent().size()).isEqualTo(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.getContent().get(0).userRequest()).isEqualTo("문 앞에 놔주세요");
        assertThat(response.getContent().get(1).userRequest()).isEqualTo("벨 눌러주세요");

        // 콘솔 출력
        System.out.println("=== 광화문 주문 이력 조회 결과 ===");
        System.out.println("총 주문 수: " + response.getContent().size());
        System.out.println("다음 페이지 있음: " + response.hasNext());
        response.getContent().forEach(o -> {
            System.out.println("---");
            System.out.println("주문 상태: " + o.orderStatus());
            System.out.println("요청사항: " + o.userRequest());
            System.out.println("가게명: " + o.storeName());
        });
    }

    @Test
    @DisplayName("주문 이력 조회 - 다음 페이지 있음")
    void getOrderHistory_hasNext() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Store store = mock(Store.class);
        given(store.getName()).willReturn("광화문 순대국");

        List<Order> orders = List.of(
                Order.create(user, store, "문 앞에 놔주세요", "서울시 종로구 세종대로 172", "정부서울청사 1층"),
                Order.create(user, store, "빠르게 부탁드려요", "서울시 종로구 율곡로 10", "KT 광화문빌딩 3층")
        );

        Pageable pageable = PageRequest.of(0, 2);
        SliceImpl<Order> slice = new SliceImpl<>(orders, pageable, true); // hasNext = true

        given(orderRepository.findByUserIdAndIsDeletedFalse(userId, pageable))
                .willReturn(slice);

        // when
        Slice<OrderResponse> response = orderService.getOrdersHistory(userId, pageable);

        // then
        assertThat(response.hasNext()).isTrue();

        System.out.println("=== 다음 페이지 테스트 ===");
        System.out.println("현재 페이지: " + pageable.getPageNumber());
        System.out.println("페이지 사이즈: " + pageable.getPageSize());
        System.out.println("다음 페이지 존재: " + response.hasNext());
    }

    @Test
    @DisplayName("주문 이력 조회 - 주문 없음")
    void getOrderHistory_empty() {
        // given
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        SliceImpl<Order> slice = new SliceImpl<>(List.of(), pageable, false);

        given(orderRepository.findByUserIdAndIsDeletedFalse(userId, pageable))
                .willReturn(slice);

        // when
        Slice<OrderResponse> response = orderService.getOrdersHistory(userId, pageable);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.hasNext()).isFalse();

        System.out.println("=== 주문 없음 테스트 ===");
        System.out.println("주문 수: " + response.getContent().size());
    }

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