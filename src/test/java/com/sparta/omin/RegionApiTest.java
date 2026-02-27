package com.sparta.omin;

import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.common.config.SecurityConfig;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class RegionApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegionService regionService;

    @Test
    void post_region_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        given(regionService.create(any())).willReturn(RegionResponse.of(id, "서울"));

        mockMvc.perform(post("/api/v1/region")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"address":"서울"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.uuid").value(id.toString()))
                .andExpect(jsonPath("$.data.address").value("서울"));
    }

    @Test
    void post_region_validationError_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/region")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"address":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("validation failed"))
                .andExpect(jsonPath("$.data.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.details.address").exists());
    }

    @Test
    void get_region_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(regionService.get(id)).willThrow(new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        mockMvc.perform(get("/api/v1/region/{regionId}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data.error").value("NOT_FOUND"));
    }

    @Test
    void put_region_conflict_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        given(regionService.update(eq(id), any())).willThrow(new IllegalStateException("이미 존재하는 지역(address)입니다."));

        mockMvc.perform(put("/api/v1/region/{regionId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"address":"서울"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.data.error").value("CONFLICT"));
    }

    @Test
    void delete_region_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        willDoNothing().given(regionService).delete(id);

        mockMvc.perform(delete("/api/v1/region/{regionId}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void get_regions_returns200() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        given(regionService.list()).willReturn(List.of(
                RegionResponse.of(id1, "A"),
                RegionResponse.of(id2, "B")
        ));

        mockMvc.perform(get("/api/v1/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data[0].uuid").value(id1.toString()))
                .andExpect(jsonPath("$.data[0].address").value("A"))
                .andExpect(jsonPath("$.data[1].uuid").value(id2.toString()))
                .andExpect(jsonPath("$.data[1].address").value("B"));
    }
}