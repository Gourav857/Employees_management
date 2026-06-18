package com.example.ems;

import com.example.ems.entity.User;
import com.example.ems.entity.Role;
import com.example.ems.repository.UserRepository;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EmployeesManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeesManagementApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedOnlyAdmin(UserRepository userRepository,
	                                       RoleRepository roleRepository,
	                                       EmployeeRepository employeeRepository,
	                                       LeaveRequestRepository leaveRequestRepository,
	                                       PasswordEncoder passwordEncoder) {
		return args -> {
			System.out.println("--- [RESET & SEED] Starting Fresh Admin Seeding ---");

			// 1. Safe Clean Sweep (Purana data delete karein sequence me)
			leaveRequestRepository.deleteAll();
			employeeRepository.deleteAll();
			userRepository.deleteAll();
			roleRepository.deleteAll();
			System.out.println("--- [RESET] Database records completely wiped for BCrypt sync! ---");

			// 2. Fresh Roles Creation
			Role adminRole = new Role();
			adminRole.setName("ROLE_ADMIN");
			adminRole = roleRepository.save(adminRole);

			Role empRole = new Role();
			empRole.setName("ROLE_EMPLOYEE");
			empRole = roleRepository.save(empRole);
			System.out.println("--- [SEED] Default Roles (ADMIN & EMPLOYEE) Created. ---");

			// 3. Create Fresh Strict Encrypted Admin Account
			User admin = new User();
			admin.setFirstName("System");
			admin.setLastName("Admin");
			admin.setEmail("admin@ems.com");
			// BCrypt encoder direct password123 ko encrypt karke database me store karega
			admin.setPassword(passwordEncoder.encode("password123"));
			admin.setRole(adminRole);

			userRepository.save(admin);
			System.out.println("--- [SEED] Fresh Admin (admin@ems.com) Created with BCrypt Hashing! ---");
			System.out.println("--- [SEED] Seeding Process Completed Successfully! ---");
		};
	}
}