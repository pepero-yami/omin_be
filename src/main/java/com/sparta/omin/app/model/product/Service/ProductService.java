package com.sparta.omin.app.model.product.Service;

import com.sparta.omin.app.model.ai.service.AiService;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.product.dto.ProductUpdateCommand;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProductService {

    private final AiService aiService;
    private final ProductRepository productRepository;

    /**
     * 점주가 본인의 가계에 메뉴를 등록할 수 있도록 합니다.<br>
     * 이때, 점주가 상품 설명을 직접 입력하지 않고, AI를 통해 생성하도록 했다면,
     * AI를 통해 생성된 상품 설명이 등록되도록 합니다.
     */
    @Transactional
    public void createProduct(
        ProductCreateCommand command,
        String userId
    ) {
        // 메뉴를 추가하려는 사장님이 해당 매장의 사장님인지 확인
        //TODO : Store 도메인에 service패키지 생성 확인후 검증 로직 작성

        // AI 설명 생성 옵션이 TRUE인 경우 AI 설명 생성
        String description = command.description();
        if(command.aiOption().enabled()) {
            description = aiService.generateMenuDescription(command.aiOption().userPrompt(), userId);
        }

        // TODO : Store 도메인에 Repository 패키지 또는 Service 패키지 생성 확인후 이어서 작성(Ref참조 또는 Store 참조)
        // 생성되거나 입력된 설명을 포함한 상품 정보 저장
//        try{
//            productRepository.save(Product.builder()
//                    .name(command.name())
//                    .description(description)
//                    .price(command.price())
//                    .status(command.status())
//                    .store()
//                .build()
//            );
//        } catch (Exception e) {
//            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
//        }
    }

    /**
     * 메뉴 수정 요청을 처리합니다.
     * @param productId
     * @param command
     * @param userId
     */
    @Transactional
    public void updateProduct(UUID productId, ProductUpdateCommand command, UUID userId) {
        // TODO : 요청한 사람이 점주 본인인지 확인하는 로직 추가

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> OminBusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        product.update(command);
    }
}
