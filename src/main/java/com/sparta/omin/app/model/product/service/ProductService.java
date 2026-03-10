package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.ai.service.AiService;
import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.product.dto.ProductUpdateCommand;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.List;
import java.util.UUID;
import com.sparta.omin.app.model.store.service.StoreReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ProductService {

    private final AiService aiService;
    private final ProductRepository productRepository;
    private final StoreReadService storeReadService;
    private final ProductImageService productImageService;

    /**
     * 점주가 본인의 가계에 메뉴를 등록할 수 있도록 합니다.<br>
     * 이때, 점주가 상품 설명을 직접 입력하지 않고, AI를 통해 생성하도록 했다면,
     * AI를 통해 생성된 상품 설명이 등록되도록 합니다.
     */
    @Transactional
    public void createProduct(
        ProductCreateCommand command,
        UUID userId,
        List<MultipartFile> images
    ) {
        // 메뉴를 추가하려는 사장님이 해당 매장의 사장님인지 확인
        storeReadService.validateStoreOwner(command.storeId(), userId);

        // AI 설명 생성 옵션이 TRUE인 경우 AI 설명 생성
        String description = command.description();
        if(command.aiOption().enabled()) {
            description = aiService.generateMenuDescription(command.aiOption().userPrompt(), userId);
        }

        // 생성되거나 입력된 설명을 포함한 상품 정보 저장
        Product product = productRepository.save(Product.builder()
                .name(command.name())
                .description(description)
                .price(command.price())
                .status(command.status())
                .store(storeReadService.getStoreReference(command.storeId()))
            .build()
        );

        // 상품 사진이 있는 경우 함께 저장
        productImageService.createImages(product, images);
    }

    /**
     * <b>상품 수정 요청을 처리</b>합니다.<br>
     * 수정은 아래의 순서대로 처리됩니다.<br>
     * 1. 상품 조회<br>
     * 2. 수정을 요청한 user가 점주인지 검증<br>
     * 3. 상품 정보 수정<br>
     * 4. 이미지 수정
     * @param productId
     * @param command
     * @param userId
     */
    @Transactional
    public void updateProduct(UUID productId, ProductUpdateCommand command, UUID userId) {

        // 상품 조회
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
            .orElseThrow(() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 메뉴를 수정하려는 사장님이 해당 매장의 사장님인지 확인
        UUID storeId = product.getStore().getId();
        storeReadService.validateStoreOwner(storeId, userId);

        // 상품 정보 수정
        product.update(command);

        // 상품 이미지 수정 처리
        productImageService.updateImages(product, command.imageCommands());
    }

    /**
     * 상품의 상태 변경요청을 처리합니다.<br>
     * 상품 상태 : {@link com.sparta.omin.app.model.product.code.ProductStatus}
     * @param productId
     * @param userId
     * @param status
     */
    @Transactional
    public void updateProductStatus(UUID productId, UUID userId, ProductStatus status) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
            .orElseThrow(() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 메뉴의 상태를 변경하려는 사장님이 해당 매장의 사장님인지 확인
        UUID storeId = product.getStore().getId();
        storeReadService.validateStoreOwner(storeId, userId);

        product.updateStatus(status);
    }

    /**
     * 상품을 삭제 처리합니다.(소프트 딜리트)
     * @param productId
     * @param userId
     */
    @Transactional
    public void deleteProduct(UUID productId, UUID userId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
            .orElseThrow(() -> new OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 메뉴를 삭제하려는 사장님이 해당 매장의 사장님인지 확인
        UUID storeId = product.getStore().getId();
        storeReadService.validateStoreOwner(storeId, userId);

        product.softDelete();
        productImageService.deleteAllProductImages(productId);
    }
}
