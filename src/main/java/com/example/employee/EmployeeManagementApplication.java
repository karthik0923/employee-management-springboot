package com.example.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * Why this class exists:
 * Every Spring Boot app needs one class that bootstraps the ApplicationContext.
 * {@code SpringApplication.run} starts the embedded Tomcat server, runs
 * auto-configuration, and component-scans this package and all sub-packages.
 *
 * Interview concept — {@code @SpringBootApplication} is a shortcut for three
 * annotations:
 * <ul>
 *   <li>{@code @Configuration} — this class can define {@code @Bean} methods</li>
 *   <li>{@code @EnableAutoConfiguration} — Boot configures DataSource, JPA,
 *       DispatcherServlet, Jackson, Validation, etc. from the classpath</li>
 *   <li>{@code @ComponentScan} — finds {@code @RestController}, {@code @Service},
 *       {@code @Repository}, {@code @Component} under {@code com.example.employee}</li>
 * </ul>
 */
@SpringBootApplication
public class EmployeeManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeManagementApplication.class, args);
	}

}
