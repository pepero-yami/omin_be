package com.sparta.omin.app.controller.region;

import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.service.RegionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    public ResponseEntity<RegionResponse> createRegion(@Valid @RequestBody RegionCreateRequest request) {
        RegionResponse created = regionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping //주소로 조회
    public ResponseEntity<Page<RegionResponse>> getRegions(
            @RequestParam(required = false) String keyword,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(regionService.getRegions(keyword, pageable));
    }

    @GetMapping("/{regionId}") //지역아이디로 조회
    public ResponseEntity<RegionResponse> getRegion(@PathVariable UUID regionId) {
        return ResponseEntity.ok(regionService.getRegion(regionId));
    }

    @PutMapping("/{regionId}")
    public ResponseEntity<RegionResponse> updateRegion(
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ResponseEntity.ok(regionService.updateRegion(regionId, request));
    }

    @DeleteMapping("/{regionId}")
    public ResponseEntity<Void> deleteRegion(@PathVariable UUID regionId) {
        regionService.deleteRegion(regionId);
        return ResponseEntity.noContent().build();
    }
}