package com.sqc.sos.dto.servicerequest;

import com.sqc.sos.model.ServiceRequest;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponse {
    private Long id;
    private String tableId;
    private String tableName;
    private String sessionId;
    private ServiceRequest.RequestType requestType;
    private String description;
    private ServiceRequest.RequestStatus status;
    private ServiceRequest.RequestPriority priority;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String assignedTo;
    private String notes;
}










