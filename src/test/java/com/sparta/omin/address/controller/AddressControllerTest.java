//package com.sparta.omin.address.controller;
//
//import com.sparta.omin.app.model.address.dto.AddressResponse;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@Disabled("테스트코드 수정중")
//@DisplayName("Address:Controller")
//@AutoConfigureMockMvc(addFilters = false)
//class AddressControllerTest extends AddressControllerHelper {
//
//    @Test
//    @DisplayName("새 배송지 추가 성공 (201 Created)")
//    void post_addresses_returns201() throws Exception {
//        // Given
//        // 헬퍼 메서드로 로그인 유저를 세팅하고, 서비스 응답을 설정
//        mockUser();
//        UUID id = UUID.randomUUID();
//        given(addressService.createAddress(any(), any())).willReturn(createResponse(id, "우리집", true));
//
//        // When
//        // /api/v1/me/addresses 경로로 POST 요청 보냄
//        mockMvc.perform(post(ADDRESS_BASE_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(ADDRESS_CREATE_FIXTURE))
//                .andDo(print())
//                // Then
//                // 201 상태 코드와 닉네임, 기본 배송지 여부 확인
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.nickname").value("우리집"))
//                .andExpect(jsonPath("$.isDefault").value(true));
//    }
//
//    @Test
//    @DisplayName("내 배송지 목록 조회 성공 (200 OK)")
//    void get_addresses_returns200() throws Exception {
//        mockUser();
//        // Given
//        // 두 개의 주소가 포함된 리스트를 반환하도록 설정
//        List<AddressResponse> content = List.of(
//                createResponse(UUID.randomUUID(), "집", true),
//                createResponse(UUID.randomUUID(), "회사", false)
//        );
//        Page<AddressResponse> pageResponse = new PageImpl<>(content, PageRequest.of(0, 10), 2);
//
//        given(addressService.getMyAddresses(any(), any())).willReturn(pageResponse);
//
//        // When & Then
//        // 목록의 개수가 2개인지 확인
//        mockMvc.perform(get(ADDRESS_BASE_URL))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").value(2))
//                .andExpect(jsonPath("$.content[0].nickname").value("집"));
//    }
//
//    @Test
//    @DisplayName("특정 주소를 기본 배송지로 설정 성공 (200 OK)")
//    void patch_address_default_returns200() throws Exception {
//        mockUser();
//
//        UUID id = UUID.randomUUID();
//        // Given
//        // 해당 ID의 주소가 기본 배송지로 변경된 결과를 반환
//        given(addressService.setDefaultAddress(any(), eq(id))).willReturn(createResponse(id, "기본지", true));
//
//        // When & Then
//        // PATCH 요청 후 상태 변경을 확인
//        mockMvc.perform(patch(ADDRESS_DEFAULT_URL.formatted(id)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isDefault").value(true));
//    }
//
//    @Test
//    @DisplayName("배송지 삭제 성공 (204 No Content)")
//    void delete_address_returns204() throws Exception {
//        mockUser();
//
//        UUID id = UUID.randomUUID();
//
//        // When & Then
//        // DELETE 요청 시 204 No Content가 오는지 확인
//        mockMvc.perform(delete(ADDRESS_ID_URL.formatted(id)))
//                .andDo(print())
//                .andExpect(status().isNoContent());
//    }
//
//    private AddressResponse createResponse(UUID id, String nickname, boolean isDefault) {
//        return AddressResponse.of(id, UUID.randomUUID(), nickname, "도로명", "상세",
//                new BigDecimal("37.1"), new BigDecimal("127.1"), isDefault,
//                LocalDateTime.now(), LocalDateTime.now());
//    }
//}