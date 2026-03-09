package com.sparta.omin.app.model.product.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.sparta.omin.app.model.ai.service.AiService;
import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand;
import com.sparta.omin.app.model.product.dto.ProductCreateCommand.DescriptionGenerateOption;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.repos.ProductRepository;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.service.StoreReadService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    StoreReadService storeReadService;
    @Mock
    AiService aiService;

    @InjectMocks
    ProductService productService;

    @Test
    @DisplayName("상품 저장 시 커맨드의 값이 Product 엔티티에 올바르게 매핑된다")
    void createProduct_savesProductWithCorrectFields() {
        // given
        UUID userId =  UUID.randomUUID();
        UUID storeId = UUID.randomUUID(); // 고정 UUID
        ProductCreateCommand command = new ProductCreateCommand(
            storeId,
            "아메리카노",
            "직접 입력한 설명",
            4500.0,
            ProductStatus.ON_SALE,
            new DescriptionGenerateOption(false, null)  // AI 옵션 OFF → description 그대로 사용
        );

        Store fakeStore = Store.builder().build();
        ReflectionTestUtils.setField(fakeStore, "id", storeId);

        // 검증/AI 로직 우회
        given(storeReadService.isOwnedStore(storeId, userId)).willReturn(true);
        given(storeReadService.getStoreReference(storeId)).willReturn(fakeStore);

        // when
        productService.createProduct(command, userId);

        // then — save()에 넘어간 Product 캡처
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        Product saved = captor.getValue();
        assertAll(
            () -> assertThat(saved.getName()).isEqualTo("아메리카노"),
            () -> assertThat(saved.getDescription()).isEqualTo("직접 입력한 설명"),
            () -> assertThat(saved.getPrice()).isEqualByComparingTo(4500.0),
            () -> assertThat(saved.getStatus()).isEqualTo(ProductStatus.ON_SALE),
            () -> assertThat(saved.getStore()).isEqualTo(fakeStore)
        );
    }
}