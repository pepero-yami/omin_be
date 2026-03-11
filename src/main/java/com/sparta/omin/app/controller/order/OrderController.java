package com.sparta.omin.app.controller.order;

import com.sparta.omin.app.application.OrderApplication;
import com.sparta.omin.app.model.order.dto.*;
import com.sparta.omin.app.model.order.service.OrderService;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderApplication orderApplication;

    /**
     * 손님
     */
    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@Valid @RequestBody OrderCreateRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderApplication.createOrder(user, request));
    }

    @GetMapping("/history")
    public ResponseEntity<Slice<OrderResponse>> getOrdersHistory(@AuthenticationPrincipal User user, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersHistory(user.getId(), pageable));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrderByCustomer(@AuthenticationPrincipal User user, @PathVariable UUID orderId, @Valid @RequestBody OrderUpdateRequest request) {
        return ResponseEntity.ok(orderApplication.updateOrderByCustomer(user.getId(), orderId, request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrderByCustomer(@AuthenticationPrincipal User user, @PathVariable UUID orderId) {
        orderService.cancelOrderByCustomer(user.getId(), orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 손님 & 사장님
     */
    @GetMapping("/{orderId}/details")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    /**
     * 사장님
     */
    @GetMapping
    public ResponseEntity<Slice<OrderResponse>> getOrdersByOwner(@RequestParam UUID storeId, @RequestParam(required = false) String status, @AuthenticationPrincipal User user, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderApplication.getOrdersByOwner(storeId, user.getId(), status, pageable));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable UUID orderId,
                                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderApplication.updateOrderStatus(user.getId(), orderId));
    }

    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<Void> rejectOrder(@PathVariable UUID orderId,
                                            @AuthenticationPrincipal User user) {
        orderApplication.rejectOrder(user.getId(), orderId);
        return ResponseEntity.noContent().build();
    }
}
