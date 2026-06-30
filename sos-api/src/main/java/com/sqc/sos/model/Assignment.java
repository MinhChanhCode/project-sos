package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "BINARY(16)")
    UUID employeeId;

    Long areaId;
    Long shiftId;
    LocalDateTime assignedAt;

    @PrePersist
    void prePersist() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }
}
