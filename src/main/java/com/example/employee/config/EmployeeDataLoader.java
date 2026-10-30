package com.example.employee.config;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inserts a few sample employees when the application starts.
 *
 * Why this class exists:
 * An empty H2 database makes manual API testing painful. Sample rows let you
 * call GET endpoints immediately after startup.
 *
 * How to disable:
 * Set {@code app.seed-data.enabled=false} in application.properties (or in
 * an environment variable / command-line arg). Tests set this to false.
 *
 * Interview concept — {@code CommandLineRunner}:
 * Spring runs every {@code CommandLineRunner} bean once, after the context
 * is ready and before the application starts accepting traffic in a useful
 * way. It is a common hook for seed data, cache warmup, or printing a banner.
 *
 * {@code @Value} injects a property. The {@code :true} part is the default
 * if the property is missing.
 */
@Component
public class EmployeeDataLoader implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(EmployeeDataLoader.class);

	private final EmployeeRepository employeeRepository;
	private final boolean seedDataEnabled;

	public EmployeeDataLoader(EmployeeRepository employeeRepository,
			@Value("${app.seed-data.enabled:true}") boolean seedDataEnabled) {
		this.employeeRepository = employeeRepository;
		this.seedDataEnabled = seedDataEnabled;
	}

	@Override
	public void run(String... args) {
		if (!seedDataEnabled) {
			log.info("Sample employee data is disabled (app.seed-data.enabled=false)");
			return;
		}
		if (employeeRepository.count() > 0) {
			log.info("Employees already exist; skipping sample data");
			return;
		}

		employeeRepository.saveAll(List.of(
				new Employee("Ada", "Lovelace", "ada.lovelace@example.com", "Engineering", new BigDecimal("120000.00")),
				new Employee("Grace", "Hopper", "grace.hopper@example.com", "Engineering", new BigDecimal("125000.00")),
				new Employee("Alan", "Turing", "alan.turing@example.com", "Research", new BigDecimal("118000.00")),
				new Employee("Katherine", "Johnson", "katherine.johnson@example.com", "Research", new BigDecimal("115000.00")),
				new Employee("Linus", "Torvalds", "linus.torvalds@example.com", "Engineering", new BigDecimal("130000.00")),
				new Employee("Margaret", "Hamilton", "margaret.hamilton@example.com", "Product", new BigDecimal("110000.00")),
				new Employee("Barbara", "Liskov", "barbara.liskov@example.com", "HR", new BigDecimal("95000.00")),
				new Employee("Donald", "Knuth", "donald.knuth@example.com", "Research", new BigDecimal("140000.00"))
		));
		log.info("Loaded {} sample employees", employeeRepository.count());
	}
}
