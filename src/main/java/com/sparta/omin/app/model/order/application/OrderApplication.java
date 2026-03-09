package com.sparta.omin.app.model.order.application;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.service.AddressReadService;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.cart.service.RCartService;
import com.sparta.omin.app.model.order.dto.OrderCreateRequest;
import com.sparta.omin.app.model.order.dto.OrderCreateResponse;
import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.dto.OrderUpdateRequest;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.service.OrderReadService;
import com.sparta.omin.app.model.order.service.OrderService;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.service.StoreReadService;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderApplication {

    private final OrderService orderService;
    private final OrderReadService orderReadService;
    private final RCartService cartService;
    private final AddressReadService addressReadService;
    private final StoreReadService storeReadService;

    /**
     * 주문 생성 흐름
     * 1. 카트 조회 (userId로)
     * 2. 카트 비어있으면 예외
     * 3. Order 생성(cascade를 통한 item 동시 저장)
     * 4. 주문 저장
     * 6. 카트 비우기
     */

    public OrderCreateResponse createOrder(User user, OrderCreateRequest request) {
        RCart cart = cartService.getCartInfo(user.getId());

        validateCart(cart);

        Address address = getAddress(user.getId(), request.addressId());

        Store store = storeReadService.getStoreReference(request.storeId());

        OrderCreateResponse response = orderService.createOrder(user, cart, address, store, request);

        cartService.refresh(user.getId());

        return response;
    }

    public Slice<OrderResponse> getOrdersByOwner(UUID storeId, UUID userId, Pageable pageable) {
        if (!storeReadService.isOwnedStore(storeId, userId)) {
            throw new OminBusinessException(ErrorCode.STORE_ACCESS_DENIED);
        }
        return orderService.getOrdersByOwner(storeId, pageable);
    }

    public OrderResponse updateOrderByCustomer(UUID userId, UUID orderId, OrderUpdateRequest request) {
        Address address = getAddress(userId, request.addressId());
        return orderService.updateOrderByCustomer(userId, orderId, address, request.userRequest());
    }

    public OrderResponse updateOrderStatus(UUID userId, UUID orderId) {
        Order order = getOrder(orderId);
        validateStoreOwner(order, userId, orderId);
        return orderService.updateOrderStatus(order);
    }


    public void rejectOrder(UUID userId, UUID orderId) {
        Order order = getOrder(orderId);
        validateStoreOwner(order, userId, orderId);
        orderService.rejectOrder(order);
    }

    //===== Helper Method =====
    private static void validateCart(RCart cart) {
        if (cart == null || cart.getProducts().isEmpty()) {
            throw new OminBusinessException(ErrorCode.CART_NOT_FOUND);
        }
    }

    private Address getAddress(UUID userId, UUID addressId) {
        return addressReadService.getMyAddress(userId, addressId);
    }

    private void validateStoreOwner(Order order, UUID userId, UUID orderId) {
        if (!storeReadService.isOwnedStore(order.getStore().getId(), userId)) {
            throw new OminBusinessException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }

    private Order getOrder(UUID orderId) {
        return orderReadService.getOrder(orderId);
    }

}
