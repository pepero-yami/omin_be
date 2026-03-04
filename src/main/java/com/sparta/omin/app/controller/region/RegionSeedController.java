package com.sparta.omin.app.controller.region;

import com.sparta.omin.app.model.region.service.RegionSeedService;
import com.sparta.omin.app.model.region.service.RegionSeedService.RegionSeedResult;
import com.sparta.omin.app.model.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RegionSeedController {

    private final RegionSeedService regionSeedService;

    public RegionSeedController(RegionSeedService regionSeedService) {
        this.regionSeedService = regionSeedService;
    }

    @PostMapping("/region-seeds")
    public ResponseEntity<RegionSeedResponse> seed(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RegionSeedResult result = regionSeedService.seedRegions(user.getId());
        return ResponseEntity.ok(RegionSeedResponse.from(result));
    }

    public record RegionSeedResponse(int insertedCount, int skippedCount) {
        public static RegionSeedResponse from(RegionSeedResult r) {
            return new RegionSeedResponse(r.insertedCount(), r.skippedCount());
        }
    }
}