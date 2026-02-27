package com.sparta.omin.common.init;

import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.common.util.AuditUserProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@EnableConfigurationProperties(SeedProperties.class)
public class DataInitializer implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final SeedProperties seedProperties;

    public DataInitializer(RegionRepository regionRepository, SeedProperties seedProperties) {
        this.regionRepository = regionRepository;
        this.seedProperties = seedProperties;
    }

    @Override
    public void run(String... args) {
        // TODO(init): Address 더미데이터는 user/address 도메인 작업 완료 후 추가

        if (!seedProperties.isEnabled()) {
            return;
        }

        if (regionRepository.count() > 0) {
            return;
        }

        UUID actorId = AuditUserProvider.currentUserId(); // TODO(auth): 인증 붙이면 실제 로그인 유저로 변경
        LocalDateTime now = LocalDateTime.now();

        List<String> regionAddresses = List.of(
                "서울특별시 종로구 적선동",
                "서울특별시 종로구 혜화동",
                "서울특별시 강남구 역삼동",
                "서울특별시 강남구 대치동",
                "서울특별시 마포구 서교동",
                "부산광역시 해운대구 우동",
                "부산광역시 수영구 광안동",
                "대구광역시 수성구 범어동",
                "인천광역시 연수구 송도동",
                "대전광역시 서구 둔산동",
                "광주광역시 서구 치평동",
                "울산광역시 남구 삼산동",
                "세종특별자치시 도움3로",
                "경기도 성남시 분당구 정자동",
                "경기도 수원시 영통구 영통동",
                "경기도 고양시 일산동구 장항동",
                "제주특별자치도 제주시 연동"
        );

        List<Region> regions = regionAddresses.stream()
                .map(addr -> Region.create(UUID.randomUUID(), addr, actorId, now))
                .toList();

        regionRepository.saveAll(regions);
    }
}