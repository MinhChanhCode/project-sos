package com.sqc.sos.dto.table;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableRequest {
    String name;
    Integer capacity;
    Long areaId;
    Integer posX;
    Integer posY;
    String tableStatus;
}
