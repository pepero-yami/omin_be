package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand.DescriptionGenerateOption;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ProductCreateRequest {

    @NotEmpty
    private String name;

    // ai 설명 생성 여부는 선택사항. 기본값=false로 처리
    private Boolean isDescGenerate = false;

    // isDescGenerate=true 인 경우에 프롬프트도 함께 받음.
    private String userPrompt;

    private String description;

    @NotNull
    private Double price;

    @NotNull
    private ProductStatus status;

    @NotNull
    private UUID storeId;

    public ProductCreateCommand toCommand() {

        boolean enabled = Boolean.TRUE.equals(isDescGenerate);

        return new ProductCreateCommand(
            storeId,
            name,
            description,
            price,
            status,
            new DescriptionGenerateOption(
                enabled,
                enabled ? userPrompt : null
            )
        );
    }
}
