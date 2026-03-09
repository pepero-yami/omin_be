package com.sparta.omin.app.model.store.dto.request;

import com.sparta.omin.app.model.store.code.Status;
import jakarta.validation.constraints.NotNull;

public record StoreStatusUpdateRequest(
        @NotNull
        Status status
) {
}
