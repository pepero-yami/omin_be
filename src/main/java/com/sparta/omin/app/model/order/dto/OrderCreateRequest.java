package com.sparta.omin.app.model.order.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OrderCreateRequest(
        @NotNull UUID storeId,
        @NotNull(message = "배송지는 필수입니다") UUID addressId,
        @Size(max = 200, message = "요청사항은 200자 이내여야 합니다") String userRequest
) {}