package com.example.ems.repository;

import com.example.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // User ID ke through employee dhoodhne ke liye configuration
    Optional<Employee> findByUserId(Long userId);

    // HIGH INDUSTRY FIX: Step 4 - Pagination supports lagaya taaki hazaron records scale ho sakein
    Page<Employee> findAll(Pageable pageable);
}