package com.sparta.omin.app.controller.product;

import com.sparta.omin.app.controller.product.payload.ProductCreateRequest;
import com.sparta.omin.app.controller.product.payload.ProductListResponse;
import com.sparta.omin.app.controller.product.payload.ProductDetailResponse;
import com.sparta.omin.app.controller.product.payload.ProductResponse;
import com.sparta.omin.app.model.product.service.ProductImageService;
import com.sparta.omin.app.model.product.service.ProductReadService;
import com.sparta.omin.app.model.product.service.ProductService;
import com.sparta.omin.app.controller.product.payload.ProductUpdateRequest;
import com.sparta.omin.app.controller.product.payload.ProductUpdateStatusRequest;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.product.dto.ProductUpdateCommand;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private final ProductImageService productImageService;

    /**
     * @apiNote 상품 생성 api 입니다.
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
        @RequestBody @Valid ProductCreateRequest request,
        @RequestPart(required = false) List<MultipartFile> images,
        @AuthenticationPrincipal User user
    ) {
        UUID userId = user.getId();
        ProductCreateCommand command = request.toCommand();
        productService.createProduct(command, userId, images);

        return ResponseEntity.status(HttpStatus.CREATED).body("success");
    }

    /**
     * @apiNote 상품 상세 조회 api입니다.<br>
     * 하나의 상품에 대한 데이터들을 반환합니다.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(
        @PathVariable UUID productId
    ) {
        ProductDetailResponse response = ProductDetailResponse.from(
            productReadService.getProductDetail(productId),
            productImageService.getImages(productId)
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * @apiNote 상품 목록 조회 api입니다.<br> 각 상품에 대한 데이터와 대표 사진을 반환합니다.<br>
     * 페이지네이션은 적용되어있지 않습니다.
     */
    // 100개까지는 그냥 내려도 될거 같은데 굳이...? 한 매장의 메뉴개수가 100개가 넘어가는 경우가 거의 없을듯
    // 꼭 넣어야 한다면, 가게별 카테고리 추가해서 카테고리 별로 내려주는 방향으로 개발
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

    /**
     * @apiNote <b>상품 수정 api</b> 입니다.<br>상품의 상태는 변경하지 않습니다.
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
        @PathVariable UUID productId,
        @RequestPart("request") @Valid ProductUpdateRequest request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files,
        @AuthenticationPrincipal User user
    ) {
        productService.updateProduct(productId, request.toCommand(files), user.getId());
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    /**
     * 상품 상태 변경 api 권한 : {@code ROLE_OWNER}
     */
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{productId}/status")
    public ResponseEntity<?> updateProductStatus(
        @RequestBody @Valid ProductUpdateStatusRequest request,
        @PathVariable UUID productId,
        @AuthenticationPrincipal User user
    ) {
        productService.updateProductStatus(productId, user.getId(), request.getProductStatus());
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    /**
     * 상품 삭제(소프트딜리트) api 권한 : {@code ROLE_OWNER}
     * @param productId
     * @param user
     * @return {@code ResponseEntity}
     */
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
        @PathVariable UUID productId,
        @AuthenticationPrincipal User user
    ) {
        productService.deleteProduct(productId, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }
}
