package com.dabwish.dabwish

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    properties = [
        "spring.data.elasticsearch.repositories.enabled=false",
        "app.elasticsearch.enabled=false",
        "app.elasticsearch.auto-reindex=false"
    ]
)
@ActiveProfiles("test")
class DabwishApplicationTests {

	@Test
	fun contextLoads() {
	}

}
