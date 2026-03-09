package com.sparta.omin.app.model.order.application;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.service.AddressReadService;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.cart.service.RCartService;
import com.sparta.omin.app.model.order.dto.OrderCreateRequest;
import com.sparta.omin.app.model.order.dto.OrderCreateResponse;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.service.OrderService;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.service.StoreReadService;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.OminBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderApplicationTest {

    @InjectMocks
    private OrderApplication orderApplication;

    @Mock
    private OrderService orderService;
    @Mock private RCartService cartService;
    @Mock private AddressReadService addressReadService;
    @Mock private StoreReadService storeReadService;

    private User mockUser;
    private RCart mockCart;
    private Address mockAddress;
    private Store mockStore;
    private OrderCreateRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(UUID.randomUUID());

        mockRequest = new OrderCreateRequest(
                UUID.randomUUID(), // addressId
                UUID.randomUUID(), // storeId
                "문 앞에 놓아주세요"
        );

        mockCart = mock(RCart.class);
        mockAddress = mock(Address.class);
        mockStore = mock(Store.class);
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        // given
        RCart.Product product = RCart.Product.builder()
                .id(UUID.randomUUID())
                .name("김치찌개")
                .price(8000.0)
                .quantity(2)
                .totalPrice(16000.0)
                .build();

        given(mockCart.getProducts()).willReturn(List.of(product));
        given(cartService.getCartInfo(mockUser.getId())).willReturn(mockCart);
        given(addressReadService.getMyAddress(mockUser.getId(), mockRequest.addressId())).willReturn(mockAddress);
        given(storeReadService.getStoreReference(mockRequest.storeId())).willReturn(mockStore);

        OrderCreateResponse expectedResponse = new OrderCreateResponse(
                UUID.randomUUID(),
                OrderStatus.PENDING,
                16000.0,
                LocalDateTime.now()
        );
        given(orderService.createOrder(mockUser, mockCart, mockAddress, mockStore, mockRequest))
                .willReturn(expectedResponse);

        // when
        OrderCreateResponse response = orderApplication.createOrder(mockUser, mockRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalPrice()).isEqualTo(16000.0);
        then(cartService).should().refresh(mockUser.getId()); // 카트 비우기 검증
    }

    @Test
    @DisplayName("카트가 비어있으면 예외 발생 - 주문 생성 안됨")
    void createOrder_emptyCart_throwsException() {
        // given
        RCart mockCart = mock(RCart.class);
        given(cartService.getCartInfo(mockUser.getId())).willReturn(mockCart);
        given(mockCart.getProducts()).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> orderApplication.createOrder(mockUser, mockRequest))
                .isInstanceOf(OminBusinessException.class);

        then(addressReadService).shouldHaveNoInteractions();
        then(storeReadService).shouldHaveNoInteractions();
        then(orderService).shouldHaveNoInteractions();
        then(cartService).should(never()).refresh(any());
    }

    @Test
    @DisplayName("카트가 null이면 예외 발생 - 주문 생성 안됨")
    void createOrder_nullCart_throwsException() {
        // given
        given(cartService.getCartInfo(mockUser.getId())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderApplication.createOrder(mockUser, mockRequest))
                .isInstanceOf(OminBusinessException.class);

        then(addressReadService).shouldHaveNoInteractions();
        then(storeReadService).shouldHaveNoInteractions();
        then(orderService).shouldHaveNoInteractions();
        then(cartService).should(never()).refresh(any());
    }

}