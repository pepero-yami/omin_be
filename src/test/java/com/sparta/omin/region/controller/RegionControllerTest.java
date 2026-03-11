//package com.sparta.omin.region.controller;
//
//import com.sparta.omin.app.model.region.dto.RegionResponse;
//import com.sparta.omin.app.model.region.service.RegionSeedService;
//import com.sparta.omin.common.error.OminBusinessException;
//import com.sparta.omin.common.error.constants.ErrorCode;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.http.MediaType;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.willDoNothing;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@Disabled("테스트코드 수정중")
//@DisplayName("Region:Controller")
//@AutoConfigureMockMvc(addFilters = false) // 보안 필터(Security) 해제 - API 로직 검증 위주
//class RegionControllerTest extends RegionControllerHelper {
//
//    @Test
//    @DisplayName("새로운 지역 등록 성공 (201 Created)")
//    void post_regions_returns201() throws Exception {
//        // Given
//        // 가짜 ID를 생성하고, 서비스가 "서울" 지역 응답을 반환하도록 설정
//        UUID id = UUID.randomUUID(); given(regionService.createRegion(any())).willReturn(RegionResponse.of(id, "서울"));
//
//        // When
//        // 요청보냄
//        var resultActions = mockMvc.perform(post(REGIONS_BASE_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(REGION_FIXTURE));
//
//        // Then
//        // 상태 코드가 201인지 확인하고, 반환된 JSON의 id와 주소가 일치하는지 검증
//        resultActions.andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.uuid").value(id.toString()))
//                .andExpect(jsonPath("$.address").value("서울"));
//    }
//
//    @Test
//    @DisplayName("필수 값 누락 시 지역 등록 실패 (400 Bad Request)")
//    void post_regions_validationError_returns400() throws Exception {
//        mockMvc.perform(post(REGIONS_BASE_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//						{"address":""}
//						"""))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
//                .andExpect(jsonPath("$.details.address").exists());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 지역 조회 시 실패 (404 Not Found)")
//    void get_regions_id_notFound_returns404() throws Exception {
//        // Given
//        // 서비스에서 REGION_NOT_FOUND 예외를 던지도록 설정
//        UUID id = UUID.randomUUID();
//        given(regionService.getRegion(id))
//                .willThrow(new OminBusinessException(ErrorCode.REGION_NOT_FOUND));
//
//        // When & Then
//        // 404 상태 코드와 에러 메시지를 확인
//        mockMvc.perform(get(REGIONS_URL_TEMPLATE.formatted("{regionId}"), id))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.error").value("REGION_NOT_FOUND"))
//                .andExpect(jsonPath("$.details").value("존재하지 않는 지역(regionId)입니다."));
//    }
//
//    @Test
//    @DisplayName("이미 존재하는 주소로 수정 시 충돌 발생 (409 Conflict)")
//    void put_regions_id_conflict_returns409() throws Exception {
//        // Given
//        // 서비스에서 이미 존재하는 지역 에러를 던지도록 설정
//        UUID id = UUID.randomUUID();
//        given(regionService.updateRegion(eq(id), any()))
//                .willThrow(new OminBusinessException(ErrorCode.REGION_ALREADY_EXISTS));
//
//        // When & Then
//        // 409 상태 코드 확인
//        mockMvc.perform(put(REGIONS_URL_TEMPLATE.formatted("{regionId}"), id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//						{"address":"서울"}
//						"""))
//                .andDo(print())
//                .andExpect(status().isConflict())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.error").value("REGION_ALREADY_EXISTS"))
//                .andExpect(jsonPath("$.details").value("이미 존재하는 지역(address)입니다."));
//    }
//
//    @Test
//    @DisplayName("지역 삭제 성공 (204 No Content)")
//    void delete_regions_id_returns204() throws Exception {
//        // Given
//        // 서비스의 삭제 로직이 아무것도 반환하지 않음을 설정
//        UUID id = UUID.randomUUID();
//        willDoNothing().given(regionService).deleteRegion(id);
//
//        // When & Then
//        // 성공 시 응답 본문이 없는 204 상태 코드를 확인
//        mockMvc.perform(delete(REGIONS_URL_TEMPLATE.formatted("{regionId}"), id))
//                .andDo(print())
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    @DisplayName("region 목록 조회 성공(200 OK)")
//    void get_regions_returns200() throws Exception {
//        UUID id1 = UUID.randomUUID();
//        UUID id2 = UUID.randomUUID();
//
//        given(regionService.getRegions()).willReturn(List.of(
//                RegionResponse.of(id1, "A"),
//                RegionResponse.of(id2, "B")
//        ));
//
//        mockMvc.perform(get(REGIONS_BASE_URL))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].uuid").value(id1.toString()))
//                .andExpect(jsonPath("$[0].address").value("A"))
//                .andExpect(jsonPath("$[1].uuid").value(id2.toString()))
//                .andExpect(jsonPath("$[1].address").value("B"));
//    }
//
//    @Test
//    @DisplayName("region seed 개수와 함께 목록 반환 성공 (200 OK)")
//    void post_regionSeeds_returns200_withCounts() throws Exception {
//        given(regionSeedService.seedRegions())
//                .willReturn(new RegionSeedService.RegionSeedResult(3, 10));
//
//        mockMvc.perform(post(REGIONS_SEEDS_URL)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.insertedCount").value(3))
//                .andExpect(jsonPath("$.skippedCount").value(10));
//    }
//
//    @Test
//    @DisplayName("키워드(강남)로 지역 검색 성공 (200 OK)")
//    void get_regions_withKeyword_returnsFilteredList() throws Exception {
//        // Given
//        // "강남" 검색 시 결과 리스트를 반환하도록
//        UUID id = UUID.randomUUID();
//        // 검색 시에는 searchRegions가 호출되어야 함
//        given(regionService.searchRegions("강남")).willReturn(List.of(
//                RegionResponse.of(id, "서울특별시 강남구 역삼동")
//        ));
//
//        // When & Then
//        // 쿼리 파라미터 keyword=강남을 전달, 결과 검증
//        mockMvc.perform(get(REGIONS_BASE_URL).param("keyword", "강남"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].address").value("서울특별시 강남구 역삼동"));
//    }
//}