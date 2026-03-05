package com.sparta.omin.region;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.service.RegionSeedService;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@DisplayName("Region:Controller")
@AutoConfigureMockMvc(addFilters = false)
class RegionControllerTest extends RegionControllerHelper {

    @Test
    void post_regions_returns201() throws Exception {
        // Given
        UUID id = UUID.randomUUID(); given(regionService.createRegion(any())).willReturn(RegionResponse.of(id, "서울"));

        // When
        var resultActions = mockMvc.perform(post(REGIONS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGION_FIXTURE));

        // Then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(id.toString()))
                .andExpect(jsonPath("$.address").value("서울"));
    }

    @Test
    void post_regions_validationError_returns400() throws Exception {
        mockMvc.perform(post(REGIONS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
						{"address":""}
						"""))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.address").exists());
    }

    @Test
    void get_regions_id_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(regionService.getRegion(id))
                .willThrow(new ApiException(ErrorCode.REGION_NOT_FOUND));

        mockMvc.perform(get(REGIONS_URL_TEMPLATE.formatted("{regionId}"), id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("REGION_NOT_FOUND"))
                .andExpect(jsonPath("$.details").value("존재하지 않는 지역(regionId)입니다."));
    }

    @Test
    void put_regions_id_conflict_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        given(regionService.updateRegion(eq(id), any()))
                .willThrow(new ApiException(ErrorCode.REGION_ALREADY_EXISTS));

        mockMvc.perform(put(REGIONS_URL_TEMPLATE.formatted("{regionId}"), id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
						{"address":"서울"}
						"""))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("REGION_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.details").value("이미 존재하는 지역(address)입니다."));
    }

    @Test
    void delete_regions_id_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        willDoNothing().given(regionService).deleteRegion(id);

        mockMvc.perform(delete(REGIONS_URL_TEMPLATE.formatted("{regionId}"), id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void get_regions_returns200() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        given(regionService.getRegions()).willReturn(List.of(
                RegionResponse.of(id1, "A"),
                RegionResponse.of(id2, "B")
        ));

        mockMvc.perform(get(REGIONS_BASE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].uuid").value(id1.toString()))
                .andExpect(jsonPath("$[0].address").value("A"))
                .andExpect(jsonPath("$[1].uuid").value(id2.toString()))
                .andExpect(jsonPath("$[1].address").value("B"));
    }

    @Test
    void post_regionSeeds_returns200_withCounts() throws Exception {
        given(regionSeedService.seedRegions())
                .willReturn(new RegionSeedService.RegionSeedResult(3, 10));

        mockMvc.perform(post(REGIONS_SEEDS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.insertedCount").value(3))
                .andExpect(jsonPath("$.skippedCount").value(10));
    }

    @Test
    void get_regions_withKeyword_returnsFilteredList() throws Exception {
        UUID id = UUID.randomUUID();
        // 검색 시에는 searchRegions가 호출되어야 함
        given(regionService.searchRegions("강남")).willReturn(List.of(
                RegionResponse.of(id, "서울특별시 강남구 역삼동")
        ));

        mockMvc.perform(get(REGIONS_BASE_URL).param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value("서울특별시 강남구 역삼동"));
    }
}