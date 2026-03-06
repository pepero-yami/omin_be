package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.repos.OrderRepository;
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

        Order order = Order.create(
                user,
                store,
                "문 앞에 놔주세요",
                "서울시 종로구 세종대로 172",
                "1층 로비"
        );

        given(orderRepository.findByIdAndIsDeletedFalse(orderId))
                .willReturn(Optional.of(order));

        // when
        OrderDetailResponse response = orderService.getOrderDetail(orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.data().orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.data().address().roadAddress()).isEqualTo("서울시 종로구 세종대로 172");
    }
}