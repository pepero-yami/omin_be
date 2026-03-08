package com.sparta.omin.app.controller.product;

import com.sparta.omin.app.controller.product.payload.ProductCreateRequest;
import com.sparta.omin.app.model.product.Service.ProductService;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품의 추가/수정/삭제 에 대한 권한은 {@code Role Owner}에게 있음
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 추가 api
     * 권한 : {@code Role Owner}
     */
//    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<?> createProduct(
        @RequestBody @Valid ProductCreateRequest request,
        @AuthenticationPrincipal User user
    ) {
        UUID userId = user.getId();
        ProductCreateCommand command = request.toCommand();
        productService.createProduct(command, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body("success");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
        @PathVariable UUID productId,
        @AuthenticationPrincipal User user
    ) {
        productService.deleteProduct(productId, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }
}
