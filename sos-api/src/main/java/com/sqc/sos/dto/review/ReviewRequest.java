package com.sqc.sos.dto.review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequest {
    UUID tableId;
    String sessionId;
    String customerName;
    Integer rating;
    String comment;
}
