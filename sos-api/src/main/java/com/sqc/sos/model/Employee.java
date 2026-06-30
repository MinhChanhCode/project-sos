package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    @Column(columnDefinition = "BINARY(16)", unique = true)
    UUID userId;

    String fullName;
    String phone;
    String email;
    String position;
    Boolean isActive;
}
