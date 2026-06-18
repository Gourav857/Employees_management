package com.example.ems.service;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveRequest;
import com.example.ems.entity.User;
import com.example.ems.entity.Role;
import com.example.ems.entity.DeletedEmployee;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveRequestRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.DeletedEmployeeRepository;
import com.example.ems.controller.NotificationController;
import org.springframework.data.domain.Page; // Added Import
import org.springframework.data.domain.PageRequest; // Added Import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeletedEmployeeRepository deletedEmployeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           LeaveRequestRepository leaveRequestRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           DeletedEmployeeRepository deletedEmployeeRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.deletedEmployeeRepository = deletedEmployeeRepository;
    }

    // --- ADMIN OPERATIONS ---

    // HIGH INDUSTRY FIX: Step 4 - List ko Page framework se badla taaki offset chunks handle ho sakein
    public Page<Employee> getAllEmployees(int page, int size) {
        System.out.println("--- [SERVICE] Fetching paginated employees for Page: " + page + ", Size: " + size + " ---");
        return employeeRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee context not found for ID: " + id));

        User associatedUser = employee.getUser();

        System.out.println("--- [ARCHIVE PROCESS] Archiving employee data into log tables... ---");

        DeletedEmployee archiveLog = new DeletedEmployee();
        archiveLog.setOriginalEmployeeId(employee.getId());
        archiveLog.setFirstName(associatedUser != null ? associatedUser.getFirstName() : "N/A");
        archiveLog.setLastName(associatedUser != null ? associatedUser.getLastName() : "N/A");
        archiveLog.setEmail(associatedUser != null ? associatedUser.getEmail() : "N/A");
        archiveLog.setDepartment(employee.getDepartment());
        archiveLog.setDesignation(employee.getDesignation());
        archiveLog.setSalary(employee.getSalary());
        archiveLog.setJoiningDate(employee.getJoiningDate());
        archiveLog.setDeletionDate(LocalDate.now());

        deletedEmployeeRepository.save(archiveLog);
        System.out.println("--- [ARCHIVE PROCESS] Successfully logged into deleted_employees table! ---");

        employeeRepository.delete(employee);

        if (associatedUser != null) {
            userRepository.delete(associatedUser);
        }

        employeeRepository.flush();
        userRepository.flush();
        deletedEmployeeRepository.flush();

        System.out.println("--- [SUCCESS] Clean wipe active records. Transferred to history logs. ---");

        NotificationController.sendRefreshSignalToAll();
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    @Transactional
    public LeaveRequest updateLeaveStatus(Long leaveId, String status) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus(status);
        LeaveRequest savedRequest = leaveRequestRepository.save(request);
        NotificationController.sendRefreshSignalToAll();
        return savedRequest;
    }

    @Transactional
    public Employee createNewEmployee(Map<String, Object> payload) {
        String email = (String) payload.get("email");

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found in database!"));

        User user = new User();
        user.setFirstName((String) payload.get("firstName"));
        user.setLastName((String) payload.get("lastName"));
        user.setEmail(email);

        String rawPassword = (String) payload.get("password");
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setRole(employeeRole);
        User savedUser = userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(savedUser);
        employee.setDepartment((String) payload.get("department"));
        employee.setDesignation((String) payload.get("designation"));

        Object salaryObj = payload.get("salary");
        Double salary = (salaryObj instanceof Number) ? ((Number) salaryObj).doubleValue() : 0.0;
        employee.setSalary(salary);

        employee.setJoiningDate(LocalDate.now());
        employee.setStatus("ACTIVE");

        Employee savedEmployee = employeeRepository.save(employee);
        NotificationController.sendRefreshSignalToAll();
        return savedEmployee;
    }

    // --- EMPLOYEE OPERATIONS ---

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getUser().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public LeaveRequest applyLeave(LeaveRequest leaveRequest) {
        leaveRequest.setStatus("PENDING");
        LeaveRequest savedLeave = leaveRequestRepository.save(leaveRequest);
        NotificationController.sendRefreshSignalToAll();
        return savedLeave;
    }

    public List<LeaveRequest> getEmployeeLeaveHistory(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }
}