package com.sparta.omin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("현재 application.yaml이 로컬 Postgres(DB_PASSWORD 필요)에 의존하여 contextLoads가 실패하므로 비활성화")
@SpringBootTest
class OminApplicationTests {

	@Test
	void contextLoads() {
	}

}
