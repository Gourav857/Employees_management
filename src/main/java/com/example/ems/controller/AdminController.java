package com.example.ems.controller;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveRequest;
import com.example.ems.service.EmployeeService;
import org.springframework.data.domain.Page; // Added Import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final EmployeeService employeeService;

    public AdminController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 1. Saare employees fetch karne ke liye (WITH PAGINATION HANDLING)
    // HIGH INDUSTRY FIX: RequestParam ke threw page and size control chunk binding lagayi
    @GetMapping("/employees")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("--- [API] Fetching paginated employees request received for Page: " + page + " ---");
        Page<Employee> employeePage = employeeService.getAllEmployees(page, size);
        return ResponseEntity.ok(employeePage);
    }

    // 2. Employee ko delete karne ke liye
    @DeleteMapping("/employee/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        System.out.println("--- [API] Request received to delete employee with ID: " + id + " ---");
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee deleted successfully"));
    }

    // 3. Saari leave requests dekhne ke liye
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequest>> getAllLeaves() {
        System.out.println("--- [API] Fetching all leave requests for Admin dashboard ---");
        return ResponseEntity.ok(employeeService.getAllLeaveRequests());
    }

    // 4. Leave status update karne ke liye (Approve/Reject)
    @PutMapping("/leave/{id}")
    public ResponseEntity<?> updateLeaveStatus(@PathVariable Long id, @RequestParam String status) {
        System.out.println("--- [API] Updating leave ID " + id + " status to: " + status + " ---");
        LeaveRequest updated = employeeService.updateLeaveStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    // 5. Naye Employee ko dashboard se register karne ke liye
    @PostMapping("/employee/add")
    public ResponseEntity<?> addEmployee(@RequestBody Map<String, Object> payload) {
        System.out.println("--- [API] Request received to register a new employee via dashboard ---");
        Employee newEmp = employeeService.createNewEmployee(payload);
        return ResponseEntity.ok(Map.of("message", "Employee and user credentials generated successfully!"));
    }
}