package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Store mockStore;    // ← @Mock으로 변경
    private User mockUser;
    private Address mockAddress;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockAddress = mock(Address.class);
    }

    @Test
    @DisplayName("주문 이력 조회 - 성공")
    void getOrderHistory_success() {
        // given
        UUID userId = UUID.randomUUID();

        Order order1 = Order.create(mockUser, mockStore, "문 앞에 놔주세요", mockAddress);
        Order order2 = Order.create(mockUser, mockStore, "벨 눌러주세요", mockAddress);

        Pageable pageable = PageRequest.of(0, 10);
        SliceImpl<Order> slice = new SliceImpl<>(List.of(order1, order2), pageable, false);

        given(orderRepository.findByUserIdWithStore(userId, pageable)).willReturn(slice);

        // when
        Slice<OrderResponse> response = orderService.getOrdersHistory(userId, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent().size()).isEqualTo(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.getContent().get(0).userRequest()).isEqualTo("문 앞에 놔주세요");
        assertThat(response.getContent().get(1).userRequest()).isEqualTo("벨 눌러주세요");

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

        List<Order> orders = List.of(
                Order.create(mockUser, mockStore, "문 앞에 놔주세요", mockAddress),
                Order.create(mockUser, mockStore, "빠르게 부탁드려요", mockAddress)
        );

        Pageable pageable = PageRequest.of(0, 2);
        SliceImpl<Order> slice = new SliceImpl<>(orders, pageable, true);

        given(orderRepository.findByUserIdWithStore(userId, pageable)).willReturn(slice);

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

        given(orderRepository.findByUserIdWithStore(userId, pageable)).willReturn(slice);

        // when
        Slice<OrderResponse> response = orderService.getOrdersHistory(userId, pageable);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.hasNext()).isFalse();

        System.out.println("=== 주문 없음 테스트 ===");
        System.out.println("주문 수: " + response.getContent().size());
    }

    @Test
    @DisplayName("주문 단건 세부 조회 - 성공")
    void getOrderDetail_success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        given(mockStore.getId()).willReturn(UUID.randomUUID());

        OrderItem orderItem = mock(OrderItem.class);
        Product product = mock(Product.class);
        given(product.getName()).willReturn("김치찌개");
        given(mockStore.getName()).willReturn("광화문 김치찌개");
        given(orderItem.getProduct()).willReturn(product);
        given(orderItem.getQuantity()).willReturn(2);
        given(orderItem.getPrice()).willReturn(8000.0);
        given(orderItem.getTotalPrice()).willReturn(16000.0);

        Order order = mock(Order.class);
        given(order.getId()).willReturn(orderId);
        given(order.getStore()).willReturn(mockStore);
        given(order.getStatus()).willReturn(OrderStatus.PENDING);
        given(order.getUserRequest()).willReturn("문 앞에 놔주세요");
        given(order.getDeliveryAddress()).willReturn("서울시 종로구 세종대로 172 정부서울청사 1층");
        given(order.getOrderItems()).willReturn(List.of(orderItem));
        given(order.getTotalPrice()).willReturn(16000.0);

        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when
        OrderDetailResponse response = orderService.getOrderDetail(orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.store().storeName()).isEqualTo("광화문 김치찌개");
        assertThat(response.deliveryAddress()).isEqualTo("서울시 종로구 세종대로 172 정부서울청사 1층");
        assertThat(response.orderItems().size()).isEqualTo(1);
        assertThat(response.orderItems().get(0).productName()).isEqualTo("김치찌개");
        assertThat(response.orderItems().get(0).quantity()).isEqualTo(2);
        assertThat(response.orderItems().get(0).itemPrice()).isEqualTo(8000.0);
        assertThat(response.orderItems().get(0).totalPrice()).isEqualTo(16000.0);

        System.out.println("=== 주문 조회 결과 ===");
        System.out.println("주문 상태: " + response.orderStatus());
        System.out.println("가게명: " + response.store().storeName());
        System.out.println("배송 주소: " + response.deliveryAddress());
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

    @Test
    @DisplayName("주문 단건 조회 - 존재하지 않으면 예외 발생")
    void getOrderDetail_notFound() {
        // given
        UUID orderId = UUID.randomUUID();
        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId))
                .isInstanceOf(OminBusinessException.class);
    }

    @Test
    @DisplayName("사장님 가게에 들어온 주문 요청 목록 조회 - 성공")
    void getOrdersByOwner_success() {
        // given
        UUID storeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "owner@gwanghwamun.com";

        List<Order> orders = List.of(
                Order.create(mockUser, mockStore, "빠르게 부탁드려요", mockAddress),
                Order.create(mockUser, mockStore, "문 앞에 놔주세요", mockAddress)
        );

        Pageable pageable = PageRequest.of(0, 10);
        SliceImpl<Order> slice = new SliceImpl<>(orders, pageable, false);

        given(orderRepository.findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(storeId, pageable))
                .willReturn(slice);

        // when
        Slice<OrderResponse> response = orderService.getOrdersByOwner(storeId, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent().size()).isEqualTo(2);
        assertThat(response.hasNext()).isFalse();

        System.out.println("=== 사장님 주문 목록 조회 결과 ===");
        System.out.println("총 주문 수: " + response.getContent().size());
        System.out.println("다음 페이지: " + response.hasNext());
        response.getContent().forEach(o -> {
            System.out.println("---");
            System.out.println("주문 상태: " + o.orderStatus());
            System.out.println("요청사항: " + o.userRequest());
        });
    }

    @Test
    @DisplayName("주문 수정 성공 - 배송지, 요청사항 변경")
    void updateOrderByCustomer_success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString("32659ae9-3ac2-4fcc-bb51-5684601bcf3c");

        given(mockUser.getId()).willReturn(userId);
        given(mockStore.getName()).willReturn("광화문 김치찌개");

        Order order = mock(Order.class);
        given(order.getUser()).willReturn(mockUser);
        given(order.getUser().getId()).willReturn(userId);
        given(order.getId()).willReturn(orderId);
        given(order.getStatus()).willReturn(OrderStatus.PENDING);
        given(order.getUserRequest()).willReturn("경비실에 맡겨주세요");
        given(order.getStore()).willReturn(mockStore);
        given(order.getTotalPrice()).willReturn(16000.0);
        given(order.getCreatedAt()).willReturn(LocalDateTime.now());
        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.updateOrderByCustomer(mockUser, orderId, mockAddress, "경비실에 맡겨주세요");

        // then
        assertThat(response).isNotNull();
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.userRequest()).isEqualTo("경비실에 맡겨주세요");
        assertThat(response.storeName()).isEqualTo("광화문 김치찌개");
        assertThat(response.totalPrice()).isEqualTo(16000.0);
        then(order).should().update(mockAddress, "경비실에 맡겨주세요");

        System.out.println("=== 주문 수정 결과 ===");
        System.out.println("주문 상태: " + response.orderStatus());
        System.out.println("요청사항: " + response.userRequest());
        System.out.println("가게명: " + response.storeName());
        System.out.println("총액: " + response.totalPrice());
    }

    @Test
    @DisplayName("주문 수정 실패 - 본인 주문 아님")
    void updateOrderByCustomer_notOwned_throwsException() {
        // given
        UUID orderId = UUID.randomUUID();
        User orderOwner = mock(User.class);

        given(mockUser.getId()).willReturn(UUID.randomUUID());   // 요청 유저
        given(orderOwner.getId()).willReturn(UUID.randomUUID()); // 주문 소유자 (다른 유저)

        Order order = mock(Order.class);
        given(order.getUser()).willReturn(orderOwner);
        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.updateOrderByCustomer(mockUser, orderId, mockAddress, "경비실에 맡겨주세요"))
                .isInstanceOf(OminBusinessException.class);

        then(order).should(never()).update(any(), any()); // update 호출 안됨
    }

    @Test
    @DisplayName("주문 수정 실패 - PENDING 아닌 상태")
    void updateOrderByCustomer_notPending_throwsException() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString("32659ae9-3ac2-4fcc-bb51-5684601bcf3c");

        given(mockUser.getId()).willReturn(userId);

        Order order = mock(Order.class);
        given(order.getUser()).willReturn(mockUser);
        given(order.getUser().getId()).willReturn(userId);
        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));
        doThrow(new OminBusinessException(ErrorCode.ORDER_UPDATE_DENIED))
                .when(order).update(any(), any());

        // when & then
        assertThatThrownBy(() -> orderService.updateOrderByCustomer(mockUser, orderId, mockAddress, "경비실에 맡겨주세요"))
                .isInstanceOf(OminBusinessException.class);

        System.out.println("=== PENDING 아닌 상태 수정 실패 검증 완료 ===");
    }

    @Test
    @DisplayName("주문 삭제 성공")
    void deleteOrderByCustomer_success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.fromString("32659ae9-3ac2-4fcc-bb51-5684601bcf3c");

        given(mockUser.getId()).willReturn(userId);

        Order order = mock(Order.class);
        given(order.getUser()).willReturn(mockUser);
        given(order.getUser().getId()).willReturn(userId);
        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when
        orderService.cancelOrderByCustomer(mockUser, orderId);

        // then
        then(order).should().cancel();

        System.out.println("=== 주문 삭제 검증 완료 ===");
    }

    @Test
    @DisplayName("주문 삭제 실패 - 본인 주문 아님")
    void deleteOrderByCustomer_notOwned_throwsException() {
        // given
        UUID orderId = UUID.randomUUID();
        User orderOwner = mock(User.class);

        given(mockUser.getId()).willReturn(UUID.randomUUID());   // 요청 유저
        given(orderOwner.getId()).willReturn(UUID.randomUUID()); // 주문 소유자 (다른 유저)

        Order order = mock(Order.class);
        given(order.getUser()).willReturn(orderOwner);
        given(orderRepository.findByIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrderByCustomer(mockUser, orderId))
                .isInstanceOf(OminBusinessException.class);

        then(order).should(never()).cancel(); // softDelete 호출 안됨

        System.out.println("=== 본인 주문 아님 삭제 실패 검증 완료 ===");
    }
}