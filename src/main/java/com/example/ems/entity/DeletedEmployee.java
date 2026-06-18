package com.example.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "deleted_employees")
@Data
@NoArgsConstructor
public class DeletedEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long originalEmployeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String designation;
    private Double salary;
    private LocalDate joiningDate;
    private LocalDate deletionDate; // Kis din company se nikala/delete kiya gaya
}
