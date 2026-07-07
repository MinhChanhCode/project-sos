package com.sqc.sos.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatResponse {
    String sessionId;
    String reply;
    String intent;
    List<Map<String, Object>> suggestedItems;
    List<Map<String, Object>> actions;
    List<String> usedTools;
    Boolean memoryUpdated;
    Boolean llmUsed;
    String llmProvider;
    String fallbackReason;
    Long historyId;
}
