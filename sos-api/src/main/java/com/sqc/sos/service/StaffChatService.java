package com.sqc.sos.service;

import com.sqc.sos.dto.chat.StaffChatRequest;
import com.sqc.sos.dto.chat.StaffChatResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.CustomerSession;
import com.sqc.sos.model.StaffChatMessage;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.repository.ICustomerSessionRepository;
import com.sqc.sos.repository.IStaffChatMessageRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffChatService {
    private final IStaffChatMessageRepository staffChatMessageRepository;
    private final ICustomerSessionRepository customerSessionRepository;
    private final ITableRepository tableRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public StaffChatResponse send(StaffChatRequest request) {
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
        String customerName = request.getCustomerName();
        if ((customerName == null || customerName.isBlank()) && request.getSessionId() != null) {
            customerName = customerSessionRepository.findBySessionId(request.getSessionId())
                    .map(CustomerSession::getCustomerName)
                    .orElse(null);
        }

        StaffChatMessage saved = staffChatMessageRepository.save(StaffChatMessage.builder()
                .tableId(table.getId())
                .sessionId(request.getSessionId())
                .customerName(customerName)
                .sender(request.getSender() == null ? "CUSTOMER" : request.getSender().toUpperCase())
                .message(request.getMessage())
                .build());
        StaffChatResponse response = toResponse(saved, table);
        Map<String, Object> payload = Map.of("type", "STAFF_CHAT_MESSAGE", "data", response);
        messagingTemplate.convertAndSend("/topic/tables/" + table.getId() + "/staff-chat", payload);
        messagingTemplate.convertAndSend("/topic/staff/chat", payload);
        return response;
    }

    @Transactional(readOnly = true)
    public List<StaffChatResponse> history(UUID tableId) {
        TableEntity table = tableRepository.findById(tableId).orElse(null);
        return staffChatMessageRepository.findTop50ByTableIdOrderByCreatedAtAsc(tableId)
                .stream()
                .map(message -> toResponse(message, table))
                .toList();
    }

    private StaffChatResponse toResponse(StaffChatMessage message, TableEntity table) {
        return StaffChatResponse.builder()
                .id(message.getId())
                .tableId(message.getTableId())
                .tableName(table != null ? table.getName() : null)
                .sessionId(message.getSessionId())
                .customerName(message.getCustomerName())
                .sender(message.getSender())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
