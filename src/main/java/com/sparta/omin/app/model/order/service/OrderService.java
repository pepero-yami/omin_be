package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.order.dto.OrderCreateRequest;
import com.sparta.omin.app.model.order.dto.OrderCreateResponse;
import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.entity.Order;
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
    public OrderResponse updateOrderByCustomer(User user, UUID orderId, Address address, String userRequest) {
        Order order = getOrder(orderId);

        if (!user.getId().equals(order.getUser().getId())) {
            throw new OminBusinessException(ErrorCode.ORDER_NOT_OWNED);
        }

        order.update(address, userRequest);

        return OrderResponse.from(order);
    }

    @Transactional
    public void deleteOrderByCustomer(User user, UUID orderId) {
        Order order = getOrder(orderId);

        if (!user.getId().equals(order.getUser().getId())) {
            throw new OminBusinessException(ErrorCode.ORDER_UPDATE_DENIED);
        }

        order.softDelete();
    }

    public OrderDetailResponse getOrderDetail(UUID orderId) {
        return OrderDetailResponse.from(getOrder(orderId));
    }

    public Slice<OrderResponse> getOrdersHistory(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdWithStore(userId, pageable)
                .map(OrderResponse::from);
    }

    public Slice<OrderResponse> getOrdersByOwner(UUID storeId, Pageable pageable) {
        return orderRepository.findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(storeId, pageable)
                .map(OrderResponse::from);
    }

    //==== Helper method ====
    private Order getOrder(UUID orderId) {
        return orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
    }


}
