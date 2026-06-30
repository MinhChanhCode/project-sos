package com.sqc.sos.dto.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftResponse {
    Long id;
    UUID staffId;
    String staffName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String role;
    Boolean isCompleted;
}
