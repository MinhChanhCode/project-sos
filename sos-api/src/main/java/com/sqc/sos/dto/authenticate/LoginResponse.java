package com.sqc.sos.dto.authenticate;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    String token;
    UUID userId;
    String username;
    String fullName;
    List<String> roles;
}
