package com.sparta.omin.app.controller.region;

import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.common.response.ApiResponse;
import com.sparta.omin.common.response.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RegionController {

    // TODO(auth): 아래 모든 Region API는 MANAGER 권한이 필요.
    //            Spring Security 도입 후 @PreAuthorize("hasRole('MANAGER')") 또는 별도 권한 체크 방식으로 보호해야 함

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PostMapping("/region")
    public ResponseEntity<ApiResponse<RegionResponse>> create(@Valid @RequestBody RegionCreateRequest request) {
        return ResponseUtil.created(regionService.create(request));
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<ApiResponse<RegionResponse>> get(@PathVariable UUID regionId) {
        return ResponseUtil.ok(regionService.get(regionId));
    }

    @PutMapping("/region/{regionId}")
    public ResponseEntity<ApiResponse<RegionResponse>> update(
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ResponseUtil.ok(regionService.update(regionId, request));
    }

    @DeleteMapping("/region/{regionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID regionId) {
        regionService.delete(regionId);
        return ResponseUtil.noContent();
    }

    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> list() {
        return ResponseUtil.ok(regionService.list());
    }
}