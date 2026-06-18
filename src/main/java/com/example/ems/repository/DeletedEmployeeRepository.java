package com.example.ems.repository;

import com.example.ems.entity.DeletedEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedEmployeeRepository extends JpaRepository<DeletedEmployee, Long> {
    // Audit repository framework handler
}
