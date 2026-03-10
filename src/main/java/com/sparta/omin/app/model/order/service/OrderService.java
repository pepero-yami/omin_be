package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.order.dto.*;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.service.ProductReadService;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductReadService productReadService;

    @Transactional
    public OrderCreateResponse createOrder(User user, RCart cart, Address address, Store store, OrderCreateRequest request) {

        List<UUID> productIds = cart.getProducts().stream()
                .map(RCart.CartItem::getId)
                .toList();

        List<Product> products = productReadService.getProductsInStore(productIds, store.getId());

        Map<UUID, Integer> quantityMap = cart.getProducts().stream()
                .collect(Collectors.toMap(RCart.CartItem::getId, RCart.CartItem::getQuantity));

        Order order = Order.create(
                user,
                store,
                request.userRequest(),
                address
        );
        order.addOrderItems(products, quantityMap);

        orderRepository.save(order);

        return OrderCreateResponse.from(order);
    }

    @Transactional
    public OrderResponse updateOrderByCustomer(UUID userId, UUID orderId, Address address, String userRequest) {
        Order order = getOrder(orderId);

        if (!userId.equals(order.getUser().getId())) {
            throw new OminBusinessException(ErrorCode.ORDER_NOT_OWNED);
        }

        order.update(address, userRequest);

        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelOrderByCustomer(UUID userId, UUID orderId) {
        Order order = getOrder(orderId);

        if (!userId.equals(order.getUser().getId())) {
            throw new OminBusinessException(ErrorCode.ORDER_UPDATE_DENIED);
        }

        order.cancel(LocalDateTime.now());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Order order) {
        order.nextStatus();
        return OrderResponse.from(order);
    }

    @Transactional
    public void rejectOrder(Order order) {
        order.reject();
    }

    public OrderDetailResponse getOrderDetail(UUID orderId) {
        return OrderDetailResponse.from(getOrder(orderId));
    }

    public Slice<OrderResponse> getOrdersHistory(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdWithStore(userId, pageable)
                .map(OrderResponse::from);
    }

    public Slice<OrderResponse> getOrdersByOwner(UUID storeId, String status, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        return orderRepository.findByStoreIdWithStatus(storeId, orderStatus, pageable)
                .map(OrderResponse::from);
    }

    //==== Helper method ====
    private Order getOrder(UUID orderId) {
        return orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
    }



    // payment 서비스에서 주문 존재 여부와 데이터를 확인하기 위한 메서드
    public Order getOrderEntity(UUID orderId) {
        return orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public OrderInternalDto getOrderForPayment(UUID orderId) {
        Order order = getOrderEntity(orderId);
        return new OrderInternalDto(
                order.getId(),
                order.getUser().getId(),
                order.getTotalPrice()
        );
    }
}
