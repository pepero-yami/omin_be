package com.sparta.omin.app.controller.order;

import com.sparta.omin.app.model.order.dto.OrderCreateRequest;
import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.service.OrderService;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * 손님
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request,
                                                     @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(user.getId(), request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersHistory(@PathVariable UUID orderId,
                                                                   @AuthenticationPrincipal User user,
                                                                   @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                                   Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersHistory(orderId, user.getId(), pageable));
    }

    /**
     * 손님 & 사장님
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    /**
     * 사장님
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrdersByOwner(@RequestParam UUID storeId,
                                                                @AuthenticationPrincipal User user,
                                                                @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                                Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByOwner(storeId, user.getId(), pageable));
    }
}
