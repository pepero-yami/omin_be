package com.sparta.omin.app.controller.region;

import com.sparta.omin.app.model.region.service.RegionSeedService;
import com.sparta.omin.app.model.region.service.RegionSeedService.RegionSeedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RegionSeedController {

    private final RegionSeedService regionSeedService;

    @PostMapping("/region-seeds")
    public ResponseEntity<RegionSeedResponse> seed() {

        RegionSeedResult result = regionSeedService.seedRegions();
        return ResponseEntity.ok(RegionSeedResponse.from(result));
    }

    public record RegionSeedResponse(int insertedCount, int skippedCount) {
        public static RegionSeedResponse from(RegionSeedResult r) {
            return new RegionSeedResponse(r.insertedCount(), r.skippedCount());
        }
    }
}