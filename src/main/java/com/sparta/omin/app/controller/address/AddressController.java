package com.sparta.omin.app.controller.address;

import com.sparta.omin.app.model.address.dto.AddressCreateRequest;
import com.sparta.omin.app.model.address.dto.AddressResponse;
import com.sparta.omin.app.model.address.dto.AddressUpdateRequest;
import com.sparta.omin.app.model.address.service.AddressService;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        AddressResponse created = addressService.createAddress(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<AddressResponse>> getAddresses(
            @AuthenticationPrincipal User user,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(addressService.getMyAddresses(user.getId(), pageable));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(addressService.getMyAddress(user.getId(), addressId));
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(addressService.updateAddress(user.getId(), addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        addressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(addressService.setDefaultAddress(user.getId(), addressId));
    }
}
