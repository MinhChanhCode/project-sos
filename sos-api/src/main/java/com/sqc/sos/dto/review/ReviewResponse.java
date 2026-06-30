package com.sqc.sos.dto.review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    Long id;
    UUID tableId;
    String tableName;
    String sessionId;
    String customerName;
    Integer rating;
    String comment;
    String sentiment;
    BigDecimal sentimentConfidence;
    LocalDateTime createdAt;
}
