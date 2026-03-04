package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.entity.Status;
import jakarta.validation.constraints.NotNull;

public record StoreStatusUpdateRequest(
        @NotNull
        Status status
        ) {
}
