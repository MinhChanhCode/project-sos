package com.sqc.sos.service;

import com.sqc.sos.dto.customer.CustomerSessionRequest;
import com.sqc.sos.dto.customer.CustomerSessionResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.CustomerSession;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.model.TableStatus;
import com.sqc.sos.repository.ICustomerSessionRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerSessionService {
    private final ICustomerSessionRepository customerSessionRepository;
    private final ITableRepository tableRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CustomerSessionResponse upsert(CustomerSessionRequest request) {
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
        String sessionId = request.getSessionId() != null && !request.getSessionId().isBlank()
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        CustomerSession session = customerSessionRepository.findBySessionId(sessionId)
                .orElse(CustomerSession.builder()
                        .sessionId(sessionId)
                        .tableId(table.getId())
                        .isActive(true)
                        .build());
        session.setTableId(table.getId());
        session.setCustomerName(request.getCustomerName());
        session.setIsActive(true);
        table.setIsAvailable(false);
        if (table.getTableStatus() == null || TableStatus.EMPTY.equals(table.getTableStatus())) {
            table.setTableStatus(TableStatus.SERVING);
        }
        tableRepository.save(table);
        eventPublisher.publishEvent(new TableStatusChangedEvent());
        return toResponse(customerSessionRepository.save(session), table);
    }

    @Transactional(readOnly = true)
    public CustomerSessionResponse get(String sessionId) {
        CustomerSession session = customerSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        TableEntity table = tableRepository.findById(session.getTableId()).orElse(null);
        return toResponse(session, table);
    }

    public CustomerSessionResponse toResponse(CustomerSession session, TableEntity table) {
        return CustomerSessionResponse.builder()
                .sessionId(session.getSessionId())
                .tableId(session.getTableId())
                .tableName(table != null ? table.getName() : null)
                .customerName(session.getCustomerName())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
