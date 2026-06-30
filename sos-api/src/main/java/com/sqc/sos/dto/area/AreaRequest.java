package com.sqc.sos.dto.area;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaRequest {
    String name;
    String description;
    Boolean isActive;
}
