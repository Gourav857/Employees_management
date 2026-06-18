package com.example.ems.repository;

import com.example.ems.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    // Ek specific employee ki saari leaves nikalne ke liye
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    // Status ke basis par leaves filter karne ke liye (Jaise Admin ko saari PENDING leaves dikhani hon)
    List<LeaveRequest> findByStatus(String status);
}
