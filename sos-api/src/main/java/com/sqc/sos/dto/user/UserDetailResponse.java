package com.sqc.sos.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailResponse {
    UUID id;
    String username;
    String fullName;
    String phone;
    String email;
    List<String> roles;
}
