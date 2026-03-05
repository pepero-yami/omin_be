package com.sparta.omin;

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

import com.sparta.omin.app.controller.region.RegionController;
import com.sparta.omin.app.controller.region.RegionSeedController;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.service.RegionSeedService;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {RegionController.class, RegionSeedController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RegionApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegionService regionService;

    @MockitoBean
    RegionSeedService regionSeedService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    void post_regions_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        given(regionService.createRegion(any())).willReturn(RegionResponse.of(id, "서울"));

        mockMvc.perform(post("/api/v1/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
						{"address":"서울"}
						"""))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(id.toString()))
                .andExpect(jsonPath("$.address").value("서울"));
    }

    @Test
    void post_regions_validationError_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/regions")
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

        mockMvc.perform(get("/api/v1/regions/{regionId}", id))
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

        mockMvc.perform(put("/api/v1/regions/{regionId}", id)
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

        mockMvc.perform(delete("/api/v1/regions/{regionId}", id))
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

        mockMvc.perform(get("/api/v1/regions"))
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

        mockMvc.perform(post("/api/v1/region-seeds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.insertedCount").value(3))
                .andExpect(jsonPath("$.skippedCount").value(10));
    }
}