package com.sparta.omin.app.controller.region;

import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regions")
public class RegionController {

    //조회(address에서 사용!)를 제외한 나머지는 다 마스터 권한 필요
    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PostMapping
    public ResponseEntity<RegionResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegionCreateRequest request
    ) {
        RegionResponse created = regionService.createRegion(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{regionId}")
    public ResponseEntity<RegionResponse> get(@PathVariable UUID regionId) {
        return ResponseEntity.ok(regionService.getRegion(regionId));
    }

    @PutMapping("/{regionId}")
    public ResponseEntity<RegionResponse> update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ResponseEntity.ok(regionService.updateRegion(regionId, request, user.getId()));
    }

    @DeleteMapping("/{regionId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable UUID regionId
    ) {
        regionService.deleteRegion(regionId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RegionResponse>> list() {
        return ResponseEntity.ok(regionService.getRegions());
    }
}