package com.sparta.omin.region.controller;

import com.sparta.omin.WebMvcTestBase;
import com.sparta.omin.app.controller.region.RegionController;
import com.sparta.omin.app.controller.region.RegionSeedController;
import com.sparta.omin.app.model.region.service.RegionSeedService;
import com.sparta.omin.app.model.region.service.RegionService;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = {RegionController.class, RegionSeedController.class})
@Disabled
public class RegionControllerHelper extends WebMvcTestBase {
    @MockitoBean
    protected RegionService regionService;

    @MockitoBean
    protected RegionSeedService regionSeedService;

    protected final String REGIONS_BASE_URL = "/api/v1/regions";
    protected final String REGIONS_SEEDS_URL = "/api/v1/region-seeds";
    protected final String REGIONS_URL_TEMPLATE = "/api/v1/regions/%s";

    protected final String REGION_FIXTURE = """
						{"address":"서울"}
						""";
}
