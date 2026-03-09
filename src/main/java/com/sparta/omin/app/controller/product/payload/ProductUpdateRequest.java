package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.dto.ProductUpdateCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ProductUpdateRequest {

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    private Double price;

    private List<UUID> imgIds;

    public ProductUpdateCommand toCommand() {

        return new ProductUpdateCommand(
            name,
            description,
            price,
            imgIds
        );
    }
}
