package com.sparta.omin.app.model.cart.service;

import com.sparta.omin.app.model.cart.dto.CartCreateRequest;
import com.sparta.omin.app.model.cart.dto.CartResponse;
import com.sparta.omin.app.model.cart.repos.CartRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public CartResponse create(@Valid CartCreateRequest request) {
        return null;
    }
}
