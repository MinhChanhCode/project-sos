package com.sqc.sos.service;

import com.sqc.sos.dto.servicerequest.ServiceRequestRequest;
import com.sqc.sos.dto.servicerequest.ServiceRequestResponse;
import com.sqc.sos.dto.page.PageResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.mapper.IServiceRequestMapper;
import com.sqc.sos.model.Cart;
import com.sqc.sos.model.ServiceRequest;
import com.sqc.sos.repository.ICartRepository;
import com.sqc.sos.repository.IServiceRequestRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServiceRequestService implements IServiceRequestService {

    private final IServiceRequestRepository serviceRequestRepository;
    private final IServiceRequestMapper serviceRequestMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ITableRepository tableRepository;
    private final ICartRepository cartRepository;


    @Override
    public ServiceRequestResponse createServiceRequest(ServiceRequestRequest request) {
        ServiceRequest serviceRequest = serviceRequestMapper.toEntity(request);

        // Ensure tableName comes from TableEntity whenever tableId is provided
        try {
            if (request.getTableId() != null && !request.getTableId().isBlank()) {
                UUID tableUuid = UUID.fromString(request.getTableId());
                tableRepository.findById(tableUuid).ifPresent(table -> {
                    if (table.getName() != null && !table.getName().isBlank()) {
                        serviceRequest.setTableName(table.getName());
                    }
                });
            }
        } catch (Exception ignored) {
        }

        // cập nhật lại sessionID => Lấy từ bảng cards ra
        Cart cart = cartRepository.findAllByTableIdAndIsActiveTrue(UUID.fromString(serviceRequest.getTableId()))
                .stream()
                .max(Comparator.comparing(Cart::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        serviceRequest.setSessionId(cart.getSessionId());
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        ServiceRequestResponse response = serviceRequestMapper.toResponse(savedRequest);

        // Gửi thông báo realtime đến tất cả nhân viên
        sendServiceRequestNotification(response, "SERVICE_REQUEST_CREATED");

        // Gửi thông báo đến bàn cụ thể
        sendTableNotification(savedRequest.getTableId(), response, "SERVICE_REQUEST_CREATED");

        log.info("Service request created: {}", response.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getServiceRequestById(Long id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_REQUEST_NOT_FOUND));
        return serviceRequestMapper.toResponse(serviceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> getAllServiceRequests(Pageable pageable) {
        Page<ServiceRequest> page = serviceRequestRepository.findAll(pageable);
        return new PageResponse<>(page.map(serviceRequestMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> getServiceRequestsByStatus(ServiceRequest.RequestStatus status,
            Pageable pageable) {
        Page<ServiceRequest> page = serviceRequestRepository.findByStatusOrderByRequestedAtDesc(status, pageable);
        return new PageResponse<>(page.map(serviceRequestMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsByTable(String tableId) {
        List<ServiceRequest> requests = serviceRequestRepository.findByTableIdOrderByRequestedAtDesc(tableId);
        return requests.stream().map(serviceRequestMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsBySession(String sessionId) {
        List<ServiceRequest> requests = serviceRequestRepository.findBySessionIdOrderByRequestedAtDesc(sessionId);
        return requests.stream().map(serviceRequestMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getPendingServiceRequests() {
        List<ServiceRequest> requests = serviceRequestRepository.findPendingRequests();
        return requests.stream().map(serviceRequestMapper::toResponse).toList();
    }

    @Override
    public ServiceRequestResponse updateServiceRequestStatus(Long id, ServiceRequest.RequestStatus status,
            String assignedTo) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_REQUEST_NOT_FOUND));

        serviceRequest.setStatus(status);
        serviceRequest.setAssignedTo(assignedTo);

        // Cập nhật thời gian theo trạng thái
        if (status == ServiceRequest.RequestStatus.IN_PROGRESS && serviceRequest.getStartedAt() == null) {
            serviceRequest.setStartedAt(LocalDateTime.now());
        } else if (status == ServiceRequest.RequestStatus.DONE && serviceRequest.getCompletedAt() == null) {
            serviceRequest.setCompletedAt(LocalDateTime.now());
        }

        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);
        ServiceRequestResponse response = serviceRequestMapper.toResponse(updatedRequest);

        // Gửi thông báo realtime
        sendServiceRequestNotification(response, "SERVICE_REQUEST_UPDATED");
        sendTableNotification(serviceRequest.getTableId(), response, "SERVICE_REQUEST_UPDATED");

        log.info("Service request status updated: {} -> {}", id, status);
        return response;
    }

    @Override
    public ServiceRequestResponse assignServiceRequest(Long id, String assignedTo) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_REQUEST_NOT_FOUND));

        serviceRequest.setAssignedTo(assignedTo);
        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);
        ServiceRequestResponse response = serviceRequestMapper.toResponse(updatedRequest);

        // Gửi thông báo realtime
        sendServiceRequestNotification(response, "SERVICE_REQUEST_ASSIGNED");

        log.info("Service request assigned: {} -> {}", id, assignedTo);
        return response;
    }

    @Override
    public void deleteServiceRequest(Long id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_REQUEST_NOT_FOUND));

        serviceRequestRepository.delete(serviceRequest);

        // Gửi thông báo xóa
        Map<String, Object> deleteNotification = Map.of(
                "id", id,
                "type", "SERVICE_REQUEST_DELETED");
        messagingTemplate.convertAndSend("/topic/service-requests", deleteNotification);

        log.info("Service request deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingRequestsByTable(String tableId) {
        return serviceRequestRepository.countPendingRequestsByTable(tableId);
    }

    // Helper methods for WebSocket notifications
    private void sendServiceRequestNotification(ServiceRequestResponse response, String type) {
        Map<String, Object> notification = Map.of(
                "type", type,
                "data", response);
        messagingTemplate.convertAndSend("/topic/service-requests", notification);
    }

    private void sendTableNotification(String tableId, ServiceRequestResponse response, String type) {
        Map<String, Object> notification = Map.of(
                "type", type,
                "data", response);
        messagingTemplate.convertAndSend("/topic/tables/" + tableId, notification);
    }
}
