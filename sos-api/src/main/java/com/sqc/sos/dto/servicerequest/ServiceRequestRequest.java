package com.sqc.sos.dto.servicerequest;

import com.sqc.sos.model.ServiceRequest;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestRequest {
    private String tableId;
    private String tableName;
    private String sessionId;
    private ServiceRequest.RequestType requestType;
    private String description;
    private ServiceRequest.RequestPriority priority;
    private String notes;
}










