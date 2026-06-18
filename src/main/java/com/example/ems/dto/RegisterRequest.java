package com.example.ems.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String roleName; // "ROLE_ADMIN" ya "ROLE_EMPLOYEE"

    // Employee specific fields
    private String department;
    private String designation;
    private Double salary;
}
