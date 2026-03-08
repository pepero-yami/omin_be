package com.sparta.omin.app.controller.product;

import com.sparta.omin.app.controller.product.payload.ProductCreateRequest;
import com.sparta.omin.app.controller.product.payload.ProductListResponse;
import com.sparta.omin.app.controller.product.payload.ProductResponse;
import com.sparta.omin.app.model.product.Service.ProductReadService;
import com.sparta.omin.app.model.product.Service.ProductService;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품의 추가/수정/삭제 에 대한 권한은 {@code Role Owner}에게 있음<br>
 * 조회 권한은 모든 역할에 있음(비회원 포함)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductReadService productReadService;

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

    /**
     * 상품 상세 조회 api
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
        @PathVariable UUID productId
    ) {
        ProductResponse response = ProductResponse.from(
            productReadService.getProduct(productId)
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 상품 목록 조회 api ({@code .../products?storeId=})<br>
     * 페이지네이션 적용 x<br>
     * 100개까지는 그냥 내려도 될거 같은데 굳이...? 한 매장의 메뉴개수가 100개가 넘어가는 경우가 거의 없을듯<br>
     * 꼭 넣어야 한다면, 가게별 카테고리 추가해서 카테고리 별로 내려주는 방향으로 개발
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
        @RequestParam UUID storeId
    ) {
        ProductListResponse response = new ProductListResponse(
            productReadService.getProducts(storeId)
                .stream()
                .map(ProductResponse::from)
                .toList()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
