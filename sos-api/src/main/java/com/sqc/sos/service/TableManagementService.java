package com.sqc.sos.service;

import com.sqc.sos.dto.table.TablePositionBatchRequest;
import com.sqc.sos.dto.table.TableRequest;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.model.TableStatus;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableManagementService {
    private final ITableRepository tableRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TableEntity create(TableRequest request) {
        if (request.getName() != null && tableRepository.findFirstByNameOrderByIdAsc(request.getName()).isPresent()) {
            return tableRepository.findFirstByNameOrderByIdAsc(request.getName()).get();
        }
        TableEntity table = TableEntity.builder()
                .name(request.getName())
                .area("main")
                .capacity(request.getCapacity() != null ? request.getCapacity() : 4)
                .areaId(request.getAreaId())
                .posX(request.getPosX() != null ? request.getPosX() : 0)
                .posY(request.getPosY() != null ? request.getPosY() : 0)
                .isAvailable(true)
                .tableStatus(parseStatus(request.getTableStatus()))
                .build();
        TableEntity saved = tableRepository.save(table);
        eventPublisher.publishEvent(new TableStatusChangedEvent());
        return saved;
    }

    @Transactional
    public TableEntity update(UUID id, TableRequest request) {
        TableEntity table = find(id);
        if (request.getName() != null) {
            tableRepository.findFirstByNameOrderByIdAsc(request.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new AppException(ErrorCode.INVALID_OPERATION); });
        }
        if (request.getName() != null) table.setName(request.getName());
        if (request.getCapacity() != null) table.setCapacity(request.getCapacity());
        if (request.getAreaId() != null) table.setAreaId(request.getAreaId());
        if (request.getPosX() != null) table.setPosX(request.getPosX());
        if (request.getPosY() != null) table.setPosY(request.getPosY());
        if (request.getTableStatus() != null) table.setTableStatus(parseStatus(request.getTableStatus()));
        TableEntity saved = tableRepository.save(table);
        eventPublisher.publishEvent(new TableStatusChangedEvent());
        return saved;
    }

    @Transactional
    public void updatePositions(TablePositionBatchRequest request) {
        if (request.getPositions() == null) return;
        for (TablePositionBatchRequest.TablePositionItem item : request.getPositions()) {
            TableEntity table = find(item.getId());
            if (item.getPosX() != null) table.setPosX(item.getPosX());
            if (item.getPosY() != null) table.setPosY(item.getPosY());
            tableRepository.save(table);
        }
        eventPublisher.publishEvent(new TableStatusChangedEvent());
    }

    @Transactional
    public void delete(UUID id) {
        tableRepository.delete(find(id));
        eventPublisher.publishEvent(new TableStatusChangedEvent());
    }

    @Transactional
    public TableEntity updateStatus(UUID id, String status) {
        TableEntity table = find(id);
        table.setTableStatus(parseStatus(status));
        if (TableStatus.EMPTY.equals(table.getTableStatus())) {
            table.setIsAvailable(true);
        } else {
            table.setIsAvailable(false);
        }
        TableEntity saved = tableRepository.save(table);
        eventPublisher.publishEvent(new TableStatusChangedEvent());
        return saved;
    }

    private TableEntity find(UUID id) {
        return tableRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
    }

    private TableStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return TableStatus.EMPTY;
        try {
            return TableStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TableStatus.EMPTY;
        }
    }
}
