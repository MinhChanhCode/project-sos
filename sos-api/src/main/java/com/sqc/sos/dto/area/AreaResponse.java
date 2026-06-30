package com.sqc.sos.dto.area;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaResponse {
    Long id;
    String name;
    String description;
    Boolean isActive;
}
