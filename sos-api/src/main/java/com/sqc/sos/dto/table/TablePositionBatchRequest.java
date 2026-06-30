package com.sqc.sos.dto.table;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TablePositionBatchRequest {
    List<TablePositionItem> positions;

    @Data
    public static class TablePositionItem {
        java.util.UUID id;
        Integer posX;
        Integer posY;
    }
}
