package com.sparta.omin.address.controller;

import com.sparta.omin.app.model.address.dto.AddressResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Address:Controller")
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest extends AddressControllerHelper {

    @Test
    void post_addresses_returns201() throws Exception {
        mockUser(); //유저를 로그인 상태로 만든다 - 헬퍼에 만들어둠

        UUID id = UUID.randomUUID();
        given(addressService.create(any(), any())).willReturn(createResponse(id, "우리집", true));

        mockMvc.perform(post(ADDRESS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADDRESS_CREATE_FIXTURE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("우리집"))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void get_addresses_returns200() throws Exception {
        mockUser();

        given(addressService.getMyAddresses(any())).willReturn(List.of(
                createResponse(UUID.randomUUID(), "집", true),
                createResponse(UUID.randomUUID(), "회사", false)
        ));

        mockMvc.perform(get(ADDRESS_BASE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void patch_address_default_returns200() throws Exception {
        mockUser();

        UUID id = UUID.randomUUID();
        given(addressService.setDefault(any(), eq(id))).willReturn(createResponse(id, "기본지", true));

        mockMvc.perform(patch(ADDRESS_DEFAULT_URL.formatted(id)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void delete_address_returns204() throws Exception {
        mockUser();

        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(ADDRESS_ID_URL.formatted(id)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    private AddressResponse createResponse(UUID id, String nickname, boolean isDefault) {
        return AddressResponse.of(id, UUID.randomUUID(), nickname, "도로명", "상세",
                new BigDecimal("37.1"), new BigDecimal("127.1"), isDefault,
                LocalDateTime.now(), LocalDateTime.now());
    }
}