package com.example.employee.repository;

import com.example.employee.entity.Employee;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data-access layer for {@link Employee}.
 *
 * Why this class exists:
 * Controllers and services must not write JDBC/SQL by hand for standard CRUD.
 * Spring Data JPA creates a proxy at runtime that implements this interface
 * and translates method names / {@code @Query} methods into SQL.
 *
 * How it interacts with other layers:
 * {@code EmployeeServiceImpl} is the only class that should call this
 * repository. The controller never talks to the database directly.
 *
 * Interview concept — you do not write the class. Spring generates it.
 * {@code JpaRepository<Employee, Long>} already provides {@code save},
 * {@code findById}, {@code findAll(Pageable)}, {@code deleteById},
 * {@code existsById}, and more.
 *
 * {@code @Repository} is not required here. Spring Data marks the proxy as
 * a repository bean automatically when it extends {@code JpaRepository}.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	/**
	 * Derived query. Spring parses the method name:
	 * findBy + Department + IgnoreCase -> WHERE LOWER(department) = LOWER(?)
	 */
	List<Employee> findByDepartmentIgnoreCase(String department);

	/**
	 * JPQL query. Use this when a method name would become unreadable.
	 * {@code CONCAT} with {@code %} implements a case-insensitive contains
	 * search on first or last name.
	 */
	@Query("""
			SELECT e FROM Employee e
			WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
			   OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
			""")
	List<Employee> searchByName(@Param("name") String name);
}
