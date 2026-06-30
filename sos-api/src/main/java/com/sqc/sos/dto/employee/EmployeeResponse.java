package com.sqc.sos.dto.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    UUID id;
    UUID userId;
    String fullName;
    String phone;
    String email;
    String position;
    Boolean isActive;
}
