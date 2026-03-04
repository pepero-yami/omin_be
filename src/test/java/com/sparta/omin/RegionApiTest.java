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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sparta.omin.app.controller.region.RegionController;
import com.sparta.omin.app.controller.region.RegionSeedController;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.service.RegionSeedService;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private void setAuthentication(UUID userId) {
        // addFilters=false라 JwtFilter를 안 타므로, @AuthenticationPrincipal 주입을 위해 SecurityContext를 직접 세팅
        User user = org.mockito.Mockito.mock(User.class);
        given(user.getId()).willReturn(userId);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void post_regions_returns201() throws Exception {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        UUID id = UUID.randomUUID();
        given(regionService.createRegion(any(), eq(actorId))).willReturn(RegionResponse.of(id, "서울"));

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
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

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
        given(regionService.getRegion(id)).willThrow(new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        mockMvc.perform(get("/api/v1/regions/{regionId}", id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void put_regions_id_conflict_returns409() throws Exception {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        UUID id = UUID.randomUUID();
        given(regionService.updateRegion(eq(id), any(), eq(actorId)))
                .willThrow(new IllegalStateException("이미 존재하는 지역(address)입니다."));

        mockMvc.perform(put("/api/v1/regions/{regionId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"address":"서울"}
                                """))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void delete_regions_id_returns204() throws Exception {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        UUID id = UUID.randomUUID();
        willDoNothing().given(regionService).deleteRegion(id, actorId);

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
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        given(regionSeedService.seedRegions(eq(actorId)))
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