package com.example.employee;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test. {@code @SpringBootTest} starts the full application context
 * (controllers, services, JPA, H2). If a bean cannot be created, this fails.
 * It does not assert business behavior — see the service and controller tests.
 */
@SpringBootTest
class EmployeeManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
