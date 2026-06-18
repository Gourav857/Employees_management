package com.example.ems.service;

import com.example.ems.dto.RegisterRequest;
import com.example.ems.entity.Employee;
import com.example.ems.entity.Role;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, EmployeeRepository employeeRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String registerUser(RegisterRequest request) {
        // 1. Check karo email pehle se exist toh nahi karti
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        // 2. Role check ya create karo database me (ROLE_ADMIN / ROLE_EMPLOYEE)
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseGet(() -> roleRepository.save(new Role(request.getRoleName())));

        // 3. User create karo
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Password Hashing
        user.setRole(role);

        // 4. Agar role EMPLOYEE hai, toh employee profile bhi bano
        if ("ROLE_EMPLOYEE".equals(request.getRoleName())) {
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setDepartment(request.getDepartment());
            employee.setDesignation(request.getDesignation());
            employee.setSalary(request.getSalary());
            employee.setJoiningDate(LocalDate.now());
            employee.setStatus("ACTIVE");

            employeeRepository.save(employee); // cascade=ALL ki wajah se user bhi save ho jayega
        } else {
            userRepository.save(user); // Admin hai toh direct user table me save
        }

        return "User registered successfully!";
    }
}
