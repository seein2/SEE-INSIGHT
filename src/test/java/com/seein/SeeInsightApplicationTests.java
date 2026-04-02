package com.seein;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring 통합 테스트 - DB/Redis 등 외부 인프라가 구성된 환경에서만 실행
 */
@Disabled("외부 인프라(DB, Redis) 없이는 Spring Context 로드 불가 - CI/CD 환경에서 활성화")
@SpringBootTest
class SeeInsightApplicationTests {

	@Test
	void contextLoads() {
	}

}
