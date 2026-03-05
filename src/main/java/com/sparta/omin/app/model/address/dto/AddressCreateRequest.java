package com.sparta.omin.app.model.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(
        @NotBlank
        @Size(max = 50)
        String nickname,

        @NotBlank
        @Size(max = 300)
        String roadAddress,

        @NotBlank
        @Size(max = 100)
        String shippingDetailAddress,

        @NotNull
        Boolean isDefault
) {
}
