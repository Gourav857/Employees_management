package com.example.ems.controller;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveRequest;
import com.example.ems.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/profile")
    public ResponseEntity<Employee> getProfile(Principal principal, @RequestParam(required = false) String email) {
        String finalEmail = (principal != null) ? principal.getName() : email;
        if (finalEmail == null) {
            finalEmail = "rahul@ems.com"; // Absolute safe testing fallback
        }
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(finalEmail));
    }

    @PostMapping("/leave/apply")
    public ResponseEntity<LeaveRequest> applyLeave(@RequestBody LeaveRequest leaveRequest, Principal principal) {
        String email = (principal != null) ? principal.getName() : "rahul@ems.com";
        Employee employee = employeeService.getEmployeeByEmail(email);
        leaveRequest.setEmployee(employee);
        if (leaveRequest.getStatus() == null) {
            leaveRequest.setStatus("PENDING");
        }
        return ResponseEntity.ok(employeeService.applyLeave(leaveRequest));
    }

    // FIX: Path endpoints ko direct sync kiya backend controller me
    @GetMapping("/leave/history")
    public ResponseEntity<List<LeaveRequest>> getLeaveHistory(Principal principal) {
        String email = (principal != null) ? principal.getName() : "rahul@ems.com";
        Employee employee = employeeService.getEmployeeByEmail(email);
        return ResponseEntity.ok(employeeService.getEmployeeLeaveHistory(employee.getId()));
    }
}